package com.notifymarquee.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.notifymarquee.databinding.ItemAppFilterBinding
import com.notifymarquee.model.AppFilter

class AppFilterAdapter(private val onToggle: (Int, Boolean) -> Unit) :
    ListAdapter<AppFilter, AppFilterAdapter.VH>(object : DiffUtil.ItemCallback<AppFilter>() {
        override fun areItemsTheSame(a: AppFilter, b: AppFilter) = a.packageName == b.packageName
        override fun areContentsTheSame(a: AppFilter, b: AppFilter) = a == b
    }) {

    inner class VH(private val b: ItemAppFilterBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: AppFilter, pos: Int) {
            b.tvAppName.text = item.displayName
            b.tvPackageName.text = item.packageName
            b.switchEnabled.setOnCheckedChangeListener(null)
            b.switchEnabled.isChecked = item.isEnabled
            b.switchEnabled.setOnCheckedChangeListener { _, on -> onToggle(pos, on) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemAppFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position), position)
}
