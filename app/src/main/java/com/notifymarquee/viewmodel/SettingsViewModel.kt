package com.notifymarquee.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.notifymarquee.model.AppFilter
import com.notifymarquee.utils.PreferenceManager

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    val prefs = PreferenceManager(app)
    val appFilters = MutableLiveData<List<AppFilter>>(prefs.getAppFilters())
    val priorityContacts = MutableLiveData<Set<String>>(prefs.getPriorityContacts())

    fun toggleFilter(idx: Int, on: Boolean) {
        val list = appFilters.value?.toMutableList() ?: return
        list[idx] = list[idx].copy(isEnabled = on)
        appFilters.value = list; prefs.saveAppFilters(list)
    }

    fun addContact(name: String) {
        val s = priorityContacts.value?.toMutableSet() ?: mutableSetOf()
        s.add(name.trim()); priorityContacts.value = s; prefs.savePriorityContacts(s)
    }

    fun removeContact(name: String) {
        val s = priorityContacts.value?.toMutableSet() ?: return
        s.remove(name); priorityContacts.value = s; prefs.savePriorityContacts(s)
    }

    var textSize: Float get() = prefs.textSize; set(v) { prefs.textSize = v }
    var scrollSpeed: Int get() = prefs.scrollSpeed; set(v) { prefs.scrollSpeed = v }
    var transparency: Int get() = prefs.transparency; set(v) { prefs.transparency = v }
    var displayDuration: Long get() = prefs.displayDuration; set(v) { prefs.displayDuration = v }
    var positionY: Int get() = prefs.positionY; set(v) { prefs.positionY = v }
    var showAppName: Boolean get() = prefs.showAppName; set(v) { prefs.showAppName = v }
}
