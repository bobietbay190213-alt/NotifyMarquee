package com.notifymarquee.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appPackage: String,
    val appName: String,
    val sender: String,
    val content: String,
    val timestamp: Long,
    val isPriority: Boolean = false
)
