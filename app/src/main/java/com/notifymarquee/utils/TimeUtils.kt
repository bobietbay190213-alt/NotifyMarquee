package com.notifymarquee.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    private val dtFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val tFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun getRelativeTime(ts: Long): String {
        val d = System.currentTimeMillis() - ts
        return when {
            d < 60_000 -> "Vừa xong"
            d < 3_600_000 -> "${d / 60_000} phút trước"
            d < 86_400_000 -> tFmt.format(Date(ts))
            else -> dtFmt.format(Date(ts))
        }
    }

    fun getStartOfToday(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun formatDateTime(ts: Long): String = dtFmt.format(Date(ts))
}
