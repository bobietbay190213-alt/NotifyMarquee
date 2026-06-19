package com.notifymarquee

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class NotifyMarqueeApp : Application() {
    companion object {
        const val CHANNEL_FOREGROUND = "ch_foreground"
        const val CHANNEL_ALERTS = "ch_alerts"
    }
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(CHANNEL_FOREGROUND, "Dịch vụ nền", NotificationManager.IMPORTANCE_LOW).apply { setShowBadge(false) })
            nm.createNotificationChannel(NotificationChannel(CHANNEL_ALERTS, "Thông báo ưu tiên", NotificationManager.IMPORTANCE_HIGH).apply { enableVibration(true) })
        }
    }
}
