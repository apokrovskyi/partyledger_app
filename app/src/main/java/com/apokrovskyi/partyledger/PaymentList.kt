package com.apokrovskyi.partyledger

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.apokrovskyi.partyledger.adapters.MutableListAdapter
import com.apokrovskyi.partyledger.adapters.SwipeDeleteCallback
import com.apokrovskyi.partyledger.models.Ledger
import com.apokrovskyi.partyledger.models.Member
import com.apokrovskyi.partyledger.models.Payment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PaymentList : TitledFragment("Payments from member to member") {
    private lateinit var addButton: FloatingActionButton
    private lateinit var paymentList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addButton = getView()!!.findViewById(R.id.fab)
        paymentList = getView()!!.findViewById(R.id.itemList)
        val paymentAdapter = MutableListAdapter(Ledger.Current.payments)
        paymentList.adapter = paymentAdapter
        ItemTouchHelper(SwipeDeleteCallback(paymentAdapter)).attachToRecyclerView(paymentList)

        val dialogView = Util.inflateDialog(layoutInflater, R.layout.purchase_dialog)
        dialogView.findViewById<LinearLayout>(R.id.descriptionInput).visibility = View.GONE
        val fromS = dialogView.findViewById<Spinner>(R.id.from_spinner)
        val toS = dialogView.findViewById<Spinner>(R.id.to_spinner)
        val amountView = dialogView.findViewById<EditText>(R.id.amountEdit)

        ArrayAdapter<Member>(this.context!!, R.layout.support_simple_spinner_dropdown_item, Ledger.Current.members)
            .also { adapter ->
                fromS.adapter = adapter
                toS.adapter = adapter
            }

        val alertDialog = AlertDialog.Builder(this.context)
            .setTitle("New payment")
            .setView(dialogView)
            .setPositiveButton("Add")
            { _, _ ->
                if (fromS.selectedItemPosition < 0 || toS.selectedItemPosition < 0) {
                    Toast.makeText(this.context, "Payment source-target pair not selected", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val amount = amountView.text.toString().toIntOrNull() ?: 0

                paymentAdapter.addItem(Payment(fromS.selectedItem as Member, toS.selectedItem as Member, amount)
                )
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()

        addButton.setOnClickListener {
            alertDialog.show()
        }
    }
}