package com.apokrovskyi.partyledger

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.apokrovskyi.partyledger.adapters.MutableListAdapter
import com.apokrovskyi.partyledger.adapters.SwipeDeleteCallback
import com.apokrovskyi.partyledger.models.Group
import com.apokrovskyi.partyledger.models.Ledger
import com.apokrovskyi.partyledger.models.Member
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GroupList : TitledFragment("Party member groups") {
    private lateinit var addButton: FloatingActionButton
    private lateinit var groupList: RecyclerView

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

        val dialogView = Util.inflateDialog(layoutInflater, R.layout.textbox_dialog)

        val alertDialog = AlertDialog.Builder(this.context)
            .setTitle("New group")
            .setView(dialogView)
            .setPositiveButton("Add")
            { _, _ ->
                val name = dialogView.findViewById<EditText>(R.id.textEdit).text.toString()
                if (name.isBlank()) {
                    Toast.makeText(this.context, "No group name specified", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val potentialMembers = Ledger.Current.purchaseReceivers.toList()
                val checkedItems = BooleanArray(potentialMembers.size)

                AlertDialog.Builder(this.context)
                    .setTitle("Select members")
                    .setMultiChoiceItems(potentialMembers.map { it.toString() }.toTypedArray(), checkedItems)
                    { _, which, isChecked -> checkedItems[which] = isChecked }
                    .setPositiveButton("Add") inner@
                    { _, _ ->

                        val memberList = mutableSetOf<Member>()
                        for (i in checkedItems.indices)
                            if (checkedItems[i]) memberList.addAll(potentialMembers[i].memberList)

                        if (memberList.isEmpty()) {
                            Toast.makeText(this.context, "Member list is empty", Toast.LENGTH_SHORT).show()
                            return@inner
                        }

                        adapter.addItem(Group(name, memberList))
                    }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                    .create()
                    .show()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()

        addButton.setOnClickListener { alertDialog.show() }
    }
}