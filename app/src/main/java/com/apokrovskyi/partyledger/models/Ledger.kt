package com.apokrovskyi.partyledger.models

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apokrovskyi.partyledger.Deserializer
import com.apokrovskyi.partyledger.Serializer

class Ledger(var name: String) : DataObject {
    companion object Global {
        var Current: Ledger = Ledger("Ledger")
        val ledgers: MutableList<Ledger> = arrayListOf(Current)
        private const val PREFS = "sharedPrefs"
        private const val KEY = "trips";

        inline fun <reified T> getObjList(): Collection<T> {
            return when (T::class) {
                Member::class -> Current.members
                Group::class -> Current.purchaseReceivers
                Payment::class -> Current.payments
                Purchase::class -> Current.purchases
                else -> throw IllegalArgumentException("Wrong item class")
            } as Collection<T>
        }

        inline fun <reified T> getByRef(index: Int): T {
            return getObjList<T>().elementAt(index)
        }

        inline fun <reified T> getRef(value: T): Int {
            return getObjList<T>().indexOf(value)
        }

        fun save(context: Context): String {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences(PREFS, AppCompatActivity.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val data = Serializer()
            data.param(ledgers.size)
            for (i in ledgers.indices) {
                Current = ledgers[i]
                data.dataObj(Current)
            }
            editor.putString(KEY, data.toString())
            editor.apply()
            Toast.makeText(context, "Data saved to storage and clipboard", Toast.LENGTH_SHORT)
                .show()
            return data.toString()
        }

        fun load(context: Context, input: String? = null) {
            try {
                val data =
                    input ?: context.getSharedPreferences(PREFS, AppCompatActivity.MODE_PRIVATE)
                        .getString(KEY, "")!!
                Deserializer(data).objList(ledgers) {
                    Current = Ledger("")
                    return@objList Current
                }
                Toast.makeText(
                    context,
                    "Loaded from ${if (input == null) "storage" else "clipboard"}",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Current = Ledger("Ledger")
                ledgers.clear()
                ledgers.add(Current)
                Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        }

        fun safeDelete(item: Any?) {
            if (item == null) return

            if (item is Group) {
                Current.purchases.removeIf { it.to == item }

                if (item is Member) {
                    Current.purchases.removeIf { it.from == item || it.to == item }
                    Current.payments.removeIf { it.from == item || it.to == item }

                    // recurse groups
                    Current.groups.removeIf {
                        val remove = it.memberList.contains(item)
                        if (remove)
                            safeDelete(it)
                        return@removeIf remove
                    }
                }
            }
        }
    }

    var members: MutableList<Member> = arrayListOf()
    var groups: MutableList<Group> = arrayListOf()
    var purchases: MutableList<Purchase> = arrayListOf()
    var payments: MutableList<Payment> = arrayListOf()

    private val totalGroup = Everybody()
    val purchaseReceivers get() = members.union(groups).plus(totalGroup)
    val transactions get() = payments.union(purchases)

    override fun Load(reader: Deserializer) {
        name = reader.getString()
        reader.objList(members) { return@objList Member() }
        reader.objList(groups) { return@objList Group() }
        reader.objList(purchases) { return@objList Purchase() }
        reader.objList(payments) { return@objList Payment() }
    }

    override fun Save(writer: Serializer) {
        writer
            .param(name)
            .objList(members)
            .objList(groups)
            .objList(purchases)
            .objList(payments)
    }

    override fun toString(): String {
        return name
    }
}