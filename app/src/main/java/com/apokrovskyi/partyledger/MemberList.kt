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
import com.apokrovskyi.partyledger.models.Member
import com.apokrovskyi.partyledger.models.Ledger
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MemberList : Fragment() {
    private lateinit var addButton: FloatingActionButton
    private lateinit var memberList: RecyclerView
    private lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addButton = view.findViewById(R.id.fab)
        memberList = view.findViewById(R.id.itemList)
        val adapter = MutableListAdapter(Ledger.Current.members)
        memberList.adapter = adapter
        ItemTouchHelper(SwipeDeleteCallback(adapter)).attachToRecyclerView(memberList)

        dialog = AlertDialog.Builder(this.context)
            .setTitle("New member")
            .setView(layoutInflater.inflate(R.layout.textbox_dialog, null))
            .setPositiveButton("Add")
            { _, _ -> adapter.addItem(Member(dialog.findViewById<EditText>(R.id.textEdit).text.toString())) }
            .setNegativeButton("Cancel") { _, _ -> dialog.cancel() }
            .create()

        addButton.setOnClickListener {
            dialog.show()
            dialog.findViewById<EditText>(R.id.textEdit).text.clear();
        }
    }
}