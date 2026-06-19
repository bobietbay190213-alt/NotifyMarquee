package com.notifymarquee.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.notifymarquee.service.NotificationListener

object PermissionUtils {
    fun hasNotificationListener(ctx: Context): Boolean {
        val cn = ComponentName(ctx, NotificationListener::class.java)
        val flat = Settings.Secure.getString(ctx.contentResolver, "enabled_notification_listeners")
        return flat?.contains(cn.flattenToString()) == true
    }

    fun hasOverlay(ctx: Context) = Settings.canDrawOverlays(ctx)

    fun openNotificationListenerSettings(ctx: Context) =
        ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

    fun openOverlaySettings(ctx: Context) =
        ctx.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
