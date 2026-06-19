package com.notifymarquee.model

data class NotificationItem(
    val id: Long = 0,
    val appPackage: String,
    val appName: String,
    val sender: String,
    val content: String,
    val timestamp: Long,
    val isPriority: Boolean = false
)
