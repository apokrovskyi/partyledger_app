package com.apokrovskyi.partyledger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.apokrovskyi.partyledger.adapters.MoveItemAdapter
import com.apokrovskyi.partyledger.models.Ledger
import com.apokrovskyi.partyledger.models.Member
import com.apokrovskyi.partyledger.models.Payment
import com.apokrovskyi.partyledger.models.Purchase
import kotlin.math.min

class Total : TitledFragment("Total debt") {
    private lateinit var debtList: MutableList<Payment>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_total, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // collect all debt and sum it for every pair of people

        var debtMap = mapPurchases(Ledger.Current.transactions, true)
        debtList = collectPayments(debtMap)

        // transform back into map and shift debt

        debtMap = mapPurchases(debtList, false)
        while (true) if (!tryShiftDebt(debtMap)) break
        debtList = collectPayments(debtMap)

        val paymentList = view.findViewById<RecyclerView>(R.id.debtPayments)
        val adapter = MoveItemAdapter(debtList, Ledger.Current.payments)
        paymentList.adapter = adapter
    }

    private fun tryShiftDebt(debtMap: MutableMap<Member, MutableMap<Member, Int>>): Boolean {
        // find who to pay for
        val (payer, target) = getShiftableDebt(debtMap) ?: return false
        val payerDebt = debtMap[payer]!!
        val targetDebt = debtMap[target]!!

        // pay for him
        payFor(payer, payerDebt, target, targetDebt)

        // cleanup
        if (targetDebt.isEmpty())
            debtMap.remove(target)
        return true
    }

    private fun getShiftableDebt(debtMap: MutableMap<Member, MutableMap<Member, Int>>): Pair<Member, Member>? {
        for (memberDebt in debtMap)
            for (debtTarget in memberDebt.value)
                if (debtMap.containsKey(debtTarget.key) && debtMap[debtTarget.key]!!.isNotEmpty())
                    return Pair(memberDebt.key, debtTarget.key)
        return null
    }

    private fun payFor(
        payer: Member,
        payerDebt: MutableMap<Member, Int>,
        target: Member,
        targetDebt: MutableMap<Member, Int>
    ) {
        while (true) {
            // stop if no longer owe him
            if (!payerDebt.containsKey(target)) return

            // stop if he owes to no one
            val to = targetDebt.keys.firstOrNull() ?: return

            val payAmount = min(targetDebt[to]!!, payerDebt[target]!!)

            // take part of his debt
            targetDebt[to] = targetDebt[to]!! - payAmount

            // repay it
            if (payer != to) payerDebt[to] = payerDebt.getOrDefault(to, 0) + payAmount

            // no longer owe him
            payerDebt[target] = payerDebt[target]!! - payAmount

            // cleanup
            targetDebt.remove(to, 0)
            payerDebt.remove(target, 0)
        }
    }

    private fun mapPurchases(
        purchases: Iterable<Purchase>,
        invert: Boolean
    ): MutableMap<Member, MutableMap<Member, Int>> {
        val result = HashMap<Member, MutableMap<Member, Int>>()
        for (payment in purchases) {
            val amount = (payment.amount / payment.to.memberList.size)
            for (memberTo in payment.to.memberList) {
                if (memberTo == payment.from)
                    continue
                appendPayment(
                    result,
                    payment.from,
                    memberTo,
                    if (invert) -amount else amount,
                    invert
                )
            }
        }

        return result
    }

    private fun appendPayment(
        debtPayments: MutableMap<Member, MutableMap<Member, Int>>,
        fromMember: Member,
        toMember: Member,
        paymentAmount: Int,
        assureUnique: Boolean
    ) {
        var from = fromMember
        var to = toMember
        var amount = paymentAmount

        if (assureUnique && debtPayments.containsKey(toMember)) {
            to = fromMember
            from = toMember
            amount = -amount
        }

        if (!debtPayments.containsKey(from))
            debtPayments[from] = HashMap()
        val memberDebt = debtPayments[from]!!
        memberDebt[to] = memberDebt.getOrDefault(to, 0) + amount
    }

    private fun collectPayments(debtMap: MutableMap<Member, MutableMap<Member, Int>>): MutableList<Payment> {
        val debtPayments = ArrayList<Payment>()
        for (member in debtMap) {
            for (debt in member.value) {
                val from = member.key
                val to = debt.key
                val amount = debt.value

                if (amount == 0)
                    continue

                if (amount <= 0)
                    debtPayments.add(Payment(to, from, -amount))
                else
                    debtPayments.add(Payment(from, to, amount))
            }
        }
        return debtPayments
    }
}