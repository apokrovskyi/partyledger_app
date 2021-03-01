package com.apokrovskyi.partyledger.adapters

class MoveItemAdapter<T>(dataSet: MutableList<T>, private val targetSet: MutableList<T>) :
    MutableListAdapter<T>(dataSet) {
    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = dataSet[position].toString()
        viewHolder.itemView.setOnClickListener {
            targetSet.add(dataSet[viewHolder.adapterPosition])
            deleteItem(viewHolder.adapterPosition)
        }
    }
}