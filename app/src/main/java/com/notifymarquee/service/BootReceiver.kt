package com.notifymarquee.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.notifymarquee.utils.PermissionUtils
import com.notifymarquee.utils.PreferenceManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val a = intent.action ?: return
        if (a == Intent.ACTION_BOOT_COMPLETED
            || a == Intent.ACTION_MY_PACKAGE_REPLACED
            || a == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val prefs = PreferenceManager(ctx)
            if (!prefs.isServiceEnabled) return

            // FIX #6: Kiểm tra quyền overlay trước khi start service
            // Nếu thiếu quyền, service sẽ crash khi cố addView overlay
            if (!PermissionUtils.hasOverlay(ctx)) {
                // Tắt service flag để tránh boot lần sau lại thử
                prefs.isServiceEnabled = false
                return
            }

            val i = Intent(ctx, MarqueeOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(i)
            } else {
                ctx.startService(i)
            }
        }
    }
}
