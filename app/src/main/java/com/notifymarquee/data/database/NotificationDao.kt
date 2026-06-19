package com.notifymarquee.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(n: NotificationEntity): Long

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAll(): LiveData<List<NotificationEntity>>

    @Query("""
        SELECT * FROM notifications 
        WHERE (:q = '' OR sender LIKE '%' || :q || '%' OR content LIKE '%' || :q || '%') 
          AND (:pkg = '' OR appPackage = :pkg) 
        ORDER BY timestamp DESC
    """)
    fun search(q: String, pkg: String): LiveData<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE timestamp >= :start")
    fun todayCount(start: Long): LiveData<Int>

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    suspend fun getAllForExport(): List<NotificationEntity>

    // FIX #9: Query xóa record cũ hơn cutoff timestamp
    @Query("DELETE FROM notifications WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
