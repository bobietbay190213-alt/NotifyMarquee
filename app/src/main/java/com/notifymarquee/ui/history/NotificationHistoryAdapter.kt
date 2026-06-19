package com.notifymarquee.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.notifymarquee.data.database.NotificationEntity
import com.notifymarquee.databinding.ItemNotificationBinding
import com.notifymarquee.utils.TimeUtils

class NotificationHistoryAdapter(private val onDelete: (Long) -> Unit) :
    ListAdapter<NotificationEntity, NotificationHistoryAdapter.VH>(object : DiffUtil.ItemCallback<NotificationEntity>() {
        override fun areItemsTheSame(a: NotificationEntity, b: NotificationEntity) = a.id == b.id
        override fun areContentsTheSame(a: NotificationEntity, b: NotificationEntity) = a == b
    }) {

    inner class VH(private val b: ItemNotificationBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: NotificationEntity) {
            b.tvAppName.text = item.appName
            b.tvSender.text = item.sender
            b.tvContent.text = item.content
            b.tvTime.text = TimeUtils.getRelativeTime(item.timestamp)
            b.ivPriority.visibility = if (item.isPriority) View.VISIBLE else View.GONE
            b.btnDelete.setOnClickListener { onDelete(item.id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
