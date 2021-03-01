package com.apokrovskyi.partyledger

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.apokrovskyi.partyledger.adapters.MutableListAdapter
import com.apokrovskyi.partyledger.adapters.SwipeDeleteCallback
import com.apokrovskyi.partyledger.models.Group
import com.apokrovskyi.partyledger.models.Member
import com.apokrovskyi.partyledger.models.Ledger
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GroupList : Fragment() {
    private lateinit var addButton: FloatingActionButton
    private lateinit var groupList: RecyclerView
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
        groupList = getView()!!.findViewById(R.id.itemList)
        val adapter = MutableListAdapter(Ledger.Current.groups)
        groupList.adapter = adapter
        ItemTouchHelper(SwipeDeleteCallback(adapter)).attachToRecyclerView(groupList)


        dialog = AlertDialog.Builder(this.context)
            .setTitle("New group")
            .setView(layoutInflater.inflate(R.layout.textbox_dialog, null))
            .setPositiveButton("Add")
            { _, _ ->
                var potentialMembers = Ledger.Current.purchaseReceivers.toList()
                val checkedItems = BooleanArray(potentialMembers.size)

                AlertDialog.Builder(this.context)
                    .setTitle("Select members")
                    .setMultiChoiceItems(
                        potentialMembers.map { it.toString() }.toTypedArray(),
                        checkedItems
                    ) { _, which, isChecked ->
                        checkedItems[which] = isChecked
                    }
                    .setPositiveButton("Add")
                    { _, _ ->
                        val memberList = mutableSetOf<Member>()
                        for (i in checkedItems.indices)
                            if (checkedItems[i]) memberList.addAll(potentialMembers[i].memberList)

                        adapter.addItem(
                            Group(
                                dialog.findViewById<EditText>(R.id.textEdit).text.toString(),
                                memberList
                            )
                        )
                    }
                    .setNegativeButton("Cancel") { _, _ -> dialog.cancel() }
                    .create()
                    .show()
            }
            .setNegativeButton("Cancel") { _, _ -> dialog.cancel() }
            .create()

        addButton.setOnClickListener {
            dialog.show()
            dialog.findViewById<EditText>(R.id.textEdit).text.clear();
        }
    }
}