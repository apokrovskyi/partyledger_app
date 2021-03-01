package com.apokrovskyi.partyledger.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apokrovskyi.partyledger.R
import com.apokrovskyi.partyledger.models.Ledger

open class MutableListAdapter<T>(protected val dataSet: MutableList<T>) :
    RecyclerView.Adapter<MutableListAdapter.ViewHolder>(), DeleteAdapter {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.member_view, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = dataSet[position].toString()
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun addItem(item: T) {
        dataSet.add(item)
        notifyItemInserted(dataSet.size - 1)
    }

    override fun deleteItem(position: Int) {
        Ledger.safeDelete(dataSet.removeAt(position))
        notifyItemRemoved(position)
    }
}