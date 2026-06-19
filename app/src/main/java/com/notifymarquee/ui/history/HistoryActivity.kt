package com.notifymarquee.ui.history

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.notifymarquee.databinding.ActivityHistoryBinding
import com.notifymarquee.viewmodel.HistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class HistoryActivity : AppCompatActivity() {
    private lateinit var b: ActivityHistoryBinding
    private val vm: HistoryViewModel by viewModels()
    private lateinit var adapter: NotificationHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        b.toolbar.setNavigationOnClickListener { finish() }

        adapter = NotificationHistoryAdapter { vm.deleteById(it) }
        b.recyclerView.adapter = adapter
        b.recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        b.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) { vm.search(s?.toString() ?: "") }
            override fun afterTextChanged(s: Editable?) {}
        })

        vm.notifications.observe(this) { list ->
            adapter.submitList(list)
            b.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            b.tvCount.text = "${list.size} thông báo"
        }

        b.btnClearAll.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Xóa lịch sử")
                .setMessage("Xóa toàn bộ thông báo?")
                .setPositiveButton("Xóa") { _, _ -> vm.deleteAll() }
                .setNegativeButton("Huỷ", null).show()
        }

        b.btnExport.setOnClickListener { exportData() }
    }

    private fun exportData() {
        lifecycleScope.launch {
            val csv = vm.exportCsv()
            withContext(Dispatchers.IO) {
                val file = File(getExternalFilesDir(null), "notify_${System.currentTimeMillis()}.csv")
                file.writeText(csv, Charsets.UTF_8)
                withContext(Dispatchers.Main) {
                    val uri = androidx.core.content.FileProvider.getUriForFile(this@HistoryActivity, "${packageName}.fileprovider", file)
                    startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"; putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }, "Xuất lịch sử"))
                }
            }
        }
    }
}
