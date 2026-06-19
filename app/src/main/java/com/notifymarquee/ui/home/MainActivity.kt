package com.notifymarquee.ui.home

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.notifymarquee.R
import com.notifymarquee.databinding.ActivityMainBinding
import com.notifymarquee.ui.history.HistoryActivity
import com.notifymarquee.ui.settings.SettingsActivity
import com.notifymarquee.utils.PermissionUtils
import com.notifymarquee.viewmodel.HomeViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private val vm: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        setupObservers()
        setupListeners()
    }

    override fun onResume() { super.onResume(); vm.checkPerms(); vm.loadState() }

    private fun setupObservers() {
        vm.serviceRunning.observe(this) { on ->
            b.switchService.isChecked = on
            b.tvServiceStatus.text = if (on) "Đang hoạt động" else "Đã tắt"
            b.ivServiceIndicator.setImageResource(if (on) R.drawable.ic_circle_active else R.drawable.ic_circle_inactive)
        }
        vm.permStatus.observe(this) { s ->
            updatePerm(b.ivPermNotification, b.tvPermNotificationStatus, s.hasListener)
            updatePerm(b.ivPermOverlay, b.tvPermOverlayStatus, s.hasOverlay)
            b.switchService.isEnabled = s.allOk
            b.tvPermissionHint.visibility = if (!s.allOk) View.VISIBLE else View.GONE
        }
        vm.todayCount.observe(this) { b.tvTodayCount.text = it.toString() }
    }

    private fun setupListeners() {
        b.switchService.setOnCheckedChangeListener { _, on ->
            if (on && vm.permStatus.value?.allOk == false) {
                b.switchService.isChecked = false; showPermDialog(); return@setOnCheckedChangeListener
            }
            vm.toggleService(on)
        }
        b.cardNotificationAccess.setOnClickListener { if (vm.permStatus.value?.hasListener == false) PermissionUtils.openNotificationListenerSettings(this) }
        b.cardOverlay.setOnClickListener { if (vm.permStatus.value?.hasOverlay == false) PermissionUtils.openOverlaySettings(this) }
        b.btnHistory.setOnClickListener { startActivity(Intent(this, HistoryActivity::class.java)) }
        b.btnSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }

    private fun updatePerm(icon: android.widget.ImageView, tv: android.widget.TextView, ok: Boolean) {
        icon.setImageResource(if (ok) R.drawable.ic_check_circle else R.drawable.ic_warning)
        tv.text = if (ok) "Đã cấp phép" else "Chưa cấp — nhấn để cài đặt"
    }

    private fun showPermDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Cần cấp quyền")
            .setMessage("Vui lòng cấp:\n• Quyền lắng nghe thông báo\n• Quyền hiển thị trên ứng dụng khác")
            .setPositiveButton("Mở cài đặt") { _, _ ->
                val s = vm.permStatus.value ?: return@setPositiveButton
                if (!s.hasListener) PermissionUtils.openNotificationListenerSettings(this)
                else PermissionUtils.openOverlaySettings(this)
            }
            .setNegativeButton("Huỷ", null).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean { menuInflater.inflate(R.menu.main_menu, menu); return true }
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_history -> { startActivity(Intent(this, HistoryActivity::class.java)); true }
        R.id.action_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
        else -> super.onOptionsItemSelected(item)
    }
}
