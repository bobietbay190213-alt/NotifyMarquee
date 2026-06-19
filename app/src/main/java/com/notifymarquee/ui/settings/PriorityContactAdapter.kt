package com.notifymarquee.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.notifymarquee.databinding.ItemPriorityContactBinding

class PriorityContactAdapter(private val onRemove: (String) -> Unit) :
    ListAdapter<String, PriorityContactAdapter.VH>(object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(a: String, b: String) = a == b
        override fun areContentsTheSame(a: String, b: String) = a == b
    }) {

    inner class VH(private val b: ItemPriorityContactBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(name: String) { b.tvContactName.text = name; b.btnRemove.setOnClickListener { onRemove(name) } }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemPriorityContactBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
