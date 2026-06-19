package com.notifymarquee.data.repository

import android.content.Context
import com.notifymarquee.data.database.AppDatabase
import com.notifymarquee.data.database.NotificationEntity
import com.notifymarquee.model.NotificationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationRepository(ctx: Context) {
    private val dao = AppDatabase.get(ctx).dao()

    suspend fun insert(item: NotificationItem) = withContext(Dispatchers.IO) {
        dao.insert(
            NotificationEntity(
                appPackage = item.appPackage,
                appName = item.appName,
                sender = item.sender,
                content = item.content,
                timestamp = item.timestamp,
                isPriority = item.isPriority
            )
        )
        // FIX #9: Tự động dọn dẹp lịch sử cũ hơn 30 ngày
        val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        dao.deleteOlderThan(cutoff)
    }

    fun getAll() = dao.getAll()
    fun search(q: String, pkg: String) = dao.search(q, pkg)
    fun todayCount(start: Long) = dao.todayCount(start)
    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) { dao.deleteById(id) }
    suspend fun deleteAll() = withContext(Dispatchers.IO) { dao.deleteAll() }
    suspend fun getAllForExport() = withContext(Dispatchers.IO) { dao.getAllForExport() }
}
