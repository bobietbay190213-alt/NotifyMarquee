package com.notifymarquee.service

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.notifymarquee.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationListener : NotificationListenerService() {
    companion object {
        const val ACTION_NEW = "com.notifymarquee.NEW_NOTIFICATION"
        const val EXTRA_PKG = "pkg"
        const val EXTRA_APP = "app"
        const val EXTRA_SENDER = "sender"
        const val EXTRA_CONTENT = "content"
        const val EXTRA_TIME = "time"
        const val EXTRA_PRIORITY = "priority"

        private val IGNORED = setOf(
            "android", "com.android.systemui",
            "com.android.settings", "com.google.android.gms"
        )
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var prefs: PreferenceManager

    // FIX #4 & #5: Cache priority contacts và enabled packages
    // để tránh đọc SharedPreferences + parse JSON mỗi notification
    @Volatile private var cachedPriority: Set<String> = emptySet()
    @Volatile private var cachedEnabled: Set<String> = emptySet()
    @Volatile private var cacheValid = false

    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceManager(this)
        refreshCache()
    }

    // Gọi khi settings thay đổi (có thể gọi từ SettingsViewModel qua broadcast nếu cần)
    private fun refreshCache() {
        scope.launch {
            // Đọc prefs một lần duy nhất, cache lại
            cachedPriority = prefs.getPriorityContacts()
            cachedEnabled = prefs.getEnabledPackages()
            cacheValid = true
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        // Refresh cache khi listener kết nối lại
        refreshCache()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        if (!prefs.isServiceEnabled) return

        val pkg = sbn.packageName
        if (pkg in IGNORED) return

        // FIX #4 & #5: Dùng cache thay vì đọc prefs mỗi lần
        // Cache được refresh trong onListenerConnected và khi settings lưu
        val enabled = cachedEnabled
        if (enabled.isNotEmpty() && pkg !in enabled) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)
            ?.toString()?.takeIf { it.isNotBlank() } ?: return

        val appName = try {
            val i = packageManager.getApplicationInfo(pkg, 0)
            packageManager.getApplicationLabel(i).toString()
        } catch (e: Exception) { pkg }

        // FIX #4: Dùng cache thay vì gọi isPriorityContact() đọc prefs mỗi lần
        val isPriority = cachedPriority.contains(title)

        scope.launch {
            sendBroadcast(Intent(ACTION_NEW).apply {
                setPackage(packageName)
                putExtra(EXTRA_PKG, pkg)
                putExtra(EXTRA_APP, appName)
                putExtra(EXTRA_SENDER, title)
                putExtra(EXTRA_CONTENT, text)
                putExtra(EXTRA_TIME, sbn.postTime)
                putExtra(EXTRA_PRIORITY, isPriority)
            })
        }
    }

    // Cho phép SettingsViewModel invalidate cache khi user lưu settings
    fun invalidateCache() { refreshCache() }
}
