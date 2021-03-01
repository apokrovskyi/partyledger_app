package com.apokrovskyi.partyledger

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.apokrovskyi.partyledger.adapters.MutableListAdapter
import com.apokrovskyi.partyledger.adapters.SwipeDeleteCallback
import com.apokrovskyi.partyledger.models.Group
import com.apokrovskyi.partyledger.models.Ledger
import com.apokrovskyi.partyledger.models.Member
import com.apokrovskyi.partyledger.models.Purchase
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PurchaseList : TitledFragment("Payments from member to group") {
    private lateinit var addButton: FloatingActionButton
    private lateinit var purchaseList: RecyclerView
    private lateinit var dialog: AlertDialog

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
        purchaseList = getView()!!.findViewById(R.id.itemList)
        val purchaseAdapter = MutableListAdapter(Ledger.Current.purchases)
        purchaseList.adapter = purchaseAdapter
        ItemTouchHelper(SwipeDeleteCallback(purchaseAdapter)).attachToRecyclerView(purchaseList)

        val dialogView = Util.inflateDialog(layoutInflater, R.layout.purchase_dialog)
        val fromS = dialogView.findViewById<Spinner>(R.id.from_spinner)
        val toS = dialogView.findViewById<Spinner>(R.id.to_spinner)
        val amountView = dialogView.findViewById<EditText>(R.id.amountEdit)
        val descriptionView = dialogView.findViewById<EditText>(R.id.descriptionEdit)

        ArrayAdapter<Member>(
            this.context!!,
            R.layout.support_simple_spinner_dropdown_item,
            Ledger.Current.members
        )
            .also { adapter ->
                fromS.adapter = adapter
            }

        ArrayAdapter<Group>(
            this.context!!,
            R.layout.support_simple_spinner_dropdown_item,
            Ledger.Current.purchaseReceivers.toTypedArray()
        )
            .also { adapter ->
                toS.adapter = adapter
            }

        dialog = AlertDialog.Builder(this.context)
            .setTitle("New purchase")
            .setView(dialogView)
            .setPositiveButton("Add")
            { _, _ ->
                if (fromS.selectedItemPosition < 0 || toS.selectedItemPosition < 0) {
                    Toast.makeText(this.context, "Purchase source-target pair not selected", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val amount = amountView.text.toString().toIntOrNull() ?: 0
                val description = descriptionView.text.toString()

                purchaseAdapter.addItem(
                    Purchase(
                        fromS.selectedItem as Member,
                        toS.selectedItem as Group,
                        amount,
                        description
                    )
                )
            }
            .setNegativeButton("Cancel") { _, _ -> dialog.cancel() }
            .create()

        addButton.setOnClickListener {
            dialog.show()
        }
    }
}