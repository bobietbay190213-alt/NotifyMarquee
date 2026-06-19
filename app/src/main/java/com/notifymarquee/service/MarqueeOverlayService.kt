package com.notifymarquee.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.notifymarquee.NotifyMarqueeApp
import com.notifymarquee.R
import com.notifymarquee.data.repository.NotificationRepository
import com.notifymarquee.model.NotificationItem
import com.notifymarquee.overlay.MarqueeOverlayView
import com.notifymarquee.ui.home.MainActivity
import com.notifymarquee.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MarqueeOverlayService : Service() {
    companion object {
        const val ACTION_STOP = "stop_overlay"
        private const val NOTIF_ID = 1001
        // FIX #7: Giới hạn tối đa queue để tránh tốn RAM
        private const val MAX_QUEUE_SIZE = 30
    }

    private lateinit var wm: WindowManager
    private var overlayView: MarqueeOverlayView? = null
    private lateinit var prefs: PreferenceManager
    private lateinit var repo: NotificationRepository
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // FIX #7: Dùng ArrayDeque với giới hạn kích thước
    private val queue = ArrayDeque<NotificationItem>()

    // FIX #1: @Volatile để đảm bảo visibility giữa các thread
    @Volatile private var isShowing = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            if (intent.action != NotificationListener.ACTION_NEW) return
            val item = NotificationItem(
                appPackage = intent.getStringExtra(NotificationListener.EXTRA_PKG) ?: "",
                appName = intent.getStringExtra(NotificationListener.EXTRA_APP) ?: "",
                sender = intent.getStringExtra(NotificationListener.EXTRA_SENDER) ?: "",
                content = intent.getStringExtra(NotificationListener.EXTRA_CONTENT) ?: "",
                timestamp = intent.getLongExtra(NotificationListener.EXTRA_TIME, System.currentTimeMillis()),
                isPriority = intent.getBooleanExtra(NotificationListener.EXTRA_PRIORITY, false)
            )
            scope.launch(Dispatchers.IO) { repo.insert(item) }

            // FIX #7: Giới hạn queue size - bỏ item cũ nhất nếu đầy
            if (queue.size >= MAX_QUEUE_SIZE) {
                // Bỏ item thường (cuối queue), giữ lại priority
                val removeIdx = queue.indexOfLast { !it.isPriority }
                if (removeIdx >= 0) queue.removeAt(removeIdx)
                else queue.removeLast() // tất cả đều priority thì bỏ cuối
            }

            // FIX #1: Toàn bộ thao tác queue và isShowing trong Main thread (receiver chạy trên Main)
            if (item.isPriority) queue.addFirst(item) else queue.addLast(item)
            if (!isShowing) showNext()
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceManager(this)
        repo = NotificationRepository(this)
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        // FIX #10: RECEIVER_NOT_EXPORTED chỉ có từ API 33
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, IntentFilter(NotificationListener.ACTION_NEW), RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, IntentFilter(NotificationListener.ACTION_NEW))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopSelf(); return START_NOT_STICKY }
        startForeground(
            NOTIF_ID,
            NotificationCompat.Builder(this, NotifyMarqueeApp.CHANNEL_FOREGROUND)
                .setContentTitle("NotifyMarquee đang chạy")
                .setContentText("Đang lắng nghe thông báo")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(
                    PendingIntent.getActivity(
                        this, 0,
                        Intent(this, MainActivity::class.java),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .addAction(
                    0, "Dừng",
                    PendingIntent.getService(
                        this, 1,
                        Intent(this, MarqueeOverlayService::class.java).apply { action = ACTION_STOP },
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .setOngoing(true).setSilent(true).build()
        )
        return START_STICKY
    }

    // FIX #1: showNext() chỉ gọi từ Main thread nên isShowing an toàn
    private fun showNext() {
        val item = queue.removeFirstOrNull() ?: run { isShowing = false; return }
        isShowing = true
        removeOverlay()

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; y = prefs.positionY }

        overlayView = MarqueeOverlayView(this, prefs).apply {
            setNotification(item)
            setOnCompleteListener {
                // Callback luôn trên Main thread (handler.post trong MarqueeOverlayView)
                removeOverlay()
                showNext()
            }
        }

        try {
            wm.addView(overlayView, params)
            overlayView?.start()
        } catch (e: Exception) {
            // FIX #6: Reset isShowing đúng cách khi addView thất bại
            isShowing = false
            overlayView = null
        }
    }

    private fun removeOverlay() {
        overlayView?.let {
            if (it.isAttachedToWindow) try { wm.removeView(it) } catch (_: Exception) {}
        }
        overlayView = null
    }

    override fun onDestroy() {
        super.onDestroy()
        isShowing = false
        queue.clear()
        removeOverlay()
        try { unregisterReceiver(receiver) } catch (_: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
