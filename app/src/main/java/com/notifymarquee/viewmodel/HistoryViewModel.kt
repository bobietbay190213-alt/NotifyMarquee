package com.notifymarquee.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.notifymarquee.data.repository.NotificationRepository
import kotlinx.coroutines.launch

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = NotificationRepository(app)
    private val query = MutableLiveData("")
    private val pkgFilter = MutableLiveData("")

    val notifications = query.switchMap { q -> repo.search(q, pkgFilter.value ?: "") }

    fun search(q: String) { query.value = q }
    fun filterPkg(pkg: String) { pkgFilter.value = pkg; query.value = query.value }
    fun deleteById(id: Long) = viewModelScope.launch { repo.deleteById(id) }
    fun deleteAll() = viewModelScope.launch { repo.deleteAll() }
    suspend fun exportCsv(): String {
        val items = repo.getAllForExport()
        return buildString {
            appendLine("App,Người gửi,Nội dung,Thời gian")
            items.forEach { appendLine("\"${it.appName}\",\"${it.sender}\",\"${it.content}\",\"${it.timestamp}\"") }
        }
    }
}
