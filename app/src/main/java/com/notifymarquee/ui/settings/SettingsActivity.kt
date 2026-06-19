package com.notifymarquee.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.notifymarquee.databinding.ActivitySettingsBinding
import com.notifymarquee.viewmodel.SettingsViewModel

class SettingsActivity : AppCompatActivity() {
    private lateinit var b: ActivitySettingsBinding
    private val vm: SettingsViewModel by viewModels()
    private lateinit var filterAdapter: AppFilterAdapter
    private lateinit var contactAdapter: PriorityContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        b.toolbar.setNavigationOnClickListener { finish() }
        setupDisplay(); setupFilters(); setupContacts(); observe()
    }

    private fun seekListener(onChange: (Int) -> Unit) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(sb: SeekBar, p: Int, user: Boolean) { onChange(p) }
        override fun onStartTrackingTouch(sb: SeekBar) {}
        override fun onStopTrackingTouch(sb: SeekBar) {}
    }

    private fun setupDisplay() {
        b.seekTextSize.progress = ((vm.textSize - 10f) * 2).toInt().coerceIn(0, 20)
        b.tvTextSizeValue.text = "${vm.textSize.toInt()} sp"
        b.seekTextSize.setOnSeekBarChangeListener(seekListener { p ->
            val v = 10f + p / 2f; vm.textSize = v; b.tvTextSizeValue.text = "${v.toInt()} sp"
        })

        b.seekScrollSpeed.progress = vm.scrollSpeed - 1
        b.tvScrollSpeedValue.text = "${vm.scrollSpeed}"
        b.seekScrollSpeed.setOnSeekBarChangeListener(seekListener { p ->
            val v = p + 1; vm.scrollSpeed = v; b.tvScrollSpeedValue.text = "$v"
        })

        b.seekTransparency.progress = vm.transparency
        b.tvTransparencyValue.text = "${vm.transparency}%"
        b.seekTransparency.setOnSeekBarChangeListener(seekListener { p ->
            vm.transparency = p; b.tvTransparencyValue.text = "$p%"
        })

        b.seekDisplayDuration.progress = (vm.displayDuration / 1000 - 3).toInt().coerceIn(0, 27)
        b.tvDisplayDurationValue.text = "${vm.displayDuration / 1000} giây"
        b.seekDisplayDuration.setOnSeekBarChangeListener(seekListener { p ->
            val s = p + 3; vm.displayDuration = s * 1000L; b.tvDisplayDurationValue.text = "$s giây"
        })

        b.seekPositionY.progress = vm.positionY
        b.tvPositionYValue.text = "${vm.positionY} px"
        b.seekPositionY.setOnSeekBarChangeListener(seekListener { p ->
            vm.positionY = p; b.tvPositionYValue.text = "$p px"
        })

        b.switchShowAppName.isChecked = vm.showAppName
        b.switchShowAppName.setOnCheckedChangeListener { _, on -> vm.showAppName = on }
    }

    private fun setupFilters() {
        filterAdapter = AppFilterAdapter { idx, on -> vm.toggleFilter(idx, on) }
        b.recyclerAppFilters.adapter = filterAdapter
        b.recyclerAppFilters.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        b.recyclerAppFilters.isNestedScrollingEnabled = false
    }

    private fun setupContacts() {
        contactAdapter = PriorityContactAdapter { vm.removeContact(it) }
        b.recyclerPriorityContacts.adapter = contactAdapter
        b.recyclerPriorityContacts.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        b.recyclerPriorityContacts.isNestedScrollingEnabled = false
        b.btnAddContact.setOnClickListener {
            val input = android.widget.EditText(this).apply { hint = "Tên người gửi"; setPadding(48, 24, 48, 24) }
            MaterialAlertDialogBuilder(this).setTitle("Thêm liên hệ ưu tiên").setView(input)
                .setPositiveButton("Thêm") { _, _ -> val n = input.text.toString().trim(); if (n.isNotEmpty()) { vm.addContact(n); Toast.makeText(this, "Đã thêm", Toast.LENGTH_SHORT).show() } }
                .setNegativeButton("Huỷ", null).show()
        }
    }

    private fun observe() {
        vm.appFilters.observe(this) { filterAdapter.submitList(it.toList()) }
        vm.priorityContacts.observe(this) { set ->
            contactAdapter.submitList(set.toList())
            b.tvNoPriorityContacts.visibility = if (set.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
