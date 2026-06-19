package com.notifymarquee.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.notifymarquee.data.repository.NotificationRepository
import com.notifymarquee.service.MarqueeOverlayService
import com.notifymarquee.utils.PermissionUtils
import com.notifymarquee.utils.PreferenceManager
import com.notifymarquee.utils.TimeUtils

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    val prefs = PreferenceManager(app)
    private val repo = NotificationRepository(app)
    private val _running = MutableLiveData(false)
    val serviceRunning: LiveData<Boolean> = _running
    private val _perms = MutableLiveData<PermStatus>()
    val permStatus: LiveData<PermStatus> = _perms
    val todayCount = repo.todayCount(TimeUtils.getStartOfToday())

    fun checkPerms() {
        val ctx = getApplication<Application>()
        _perms.value = PermStatus(PermissionUtils.hasNotificationListener(ctx), PermissionUtils.hasOverlay(ctx))
    }

    fun toggleService(on: Boolean) {
        val ctx = getApplication<Application>()
        prefs.isServiceEnabled = on
        val i = Intent(ctx, MarqueeOverlayService::class.java)
        if (on) { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i) else ctx.startService(i) }
        else ctx.stopService(i)
        _running.value = on
    }

    fun loadState() { _running.value = prefs.isServiceEnabled }

    data class PermStatus(val hasListener: Boolean, val hasOverlay: Boolean) {
        val allOk get() = hasListener && hasOverlay
    }
}
