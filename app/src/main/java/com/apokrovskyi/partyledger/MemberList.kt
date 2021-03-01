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
import com.apokrovskyi.partyledger.models.Ledger
import com.apokrovskyi.partyledger.models.Member
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MemberList : TitledFragment("Party members") {
    private lateinit var addButton: FloatingActionButton
    private lateinit var memberList: RecyclerView

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

        val dialogView = Util.inflateDialog(layoutInflater, R.layout.textbox_dialog)

        val alertDialog = AlertDialog.Builder(this.context)
            .setTitle("New member")
            .setView(dialogView)
            .setPositiveButton("Add")
            { _, _ ->
                val name = dialogView.findViewById<EditText>(R.id.textEdit).text.toString();
                if (name.isBlank()) {
                    Toast.makeText(this.context, "No member name specified", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                adapter.addItem(Member())
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()

        addButton.setOnClickListener { alertDialog.show() }
    }
}