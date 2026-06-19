package com.notifymarquee.utils

import android.content.Context
import com.notifymarquee.model.AppFilter
import org.json.JSONArray
import org.json.JSONObject

class PreferenceManager(ctx: Context) {
    private val p = ctx.getSharedPreferences("nm_prefs", Context.MODE_PRIVATE)

    var isServiceEnabled: Boolean get() = p.getBoolean("svc_on", false); set(v) = p.edit().putBoolean("svc_on", v).apply()
    var textSize: Float get() = p.getFloat("txt_size", 14f); set(v) = p.edit().putFloat("txt_size", v).apply()
    var scrollSpeed: Int get() = p.getInt("speed", 5); set(v) = p.edit().putInt("speed", v).apply()
    var transparency: Int get() = p.getInt("alpha", 85); set(v) = p.edit().putInt("alpha", v).apply()
    var positionY: Int get() = p.getInt("pos_y", 100); set(v) = p.edit().putInt("pos_y", v).apply()
    var displayDuration: Long get() = p.getLong("duration", 8000L); set(v) = p.edit().putLong("duration", v).apply()
    var cornerRadius: Float get() = p.getFloat("radius", 8f); set(v) = p.edit().putFloat("radius", v).apply()
    var textColor: String get() = p.getString("txt_color", "#FFFFFF") ?: "#FFFFFF"; set(v) = p.edit().putString("txt_color", v).apply()
    var bgColor: String get() = p.getString("bg_color", "#CC1565C0") ?: "#CC1565C0"; set(v) = p.edit().putString("bg_color", v).apply()
    var showAppName: Boolean get() = p.getBoolean("show_app", true); set(v) = p.edit().putBoolean("show_app", v).apply()

    fun getAppFilters(): List<AppFilter> {
        val json = p.getString("filters", null) ?: return AppFilter.defaultFilters()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getJSONObject(it).let { o -> AppFilter(o.getString("pkg"), o.getString("name"), o.getBoolean("en")) } }
        } catch (e: Exception) { AppFilter.defaultFilters() }
    }

    fun saveAppFilters(filters: List<AppFilter>) {
        val arr = JSONArray()
        filters.forEach { arr.put(JSONObject().put("pkg", it.packageName).put("name", it.displayName).put("en", it.isEnabled)) }
        p.edit().putString("filters", arr.toString()).apply()
    }

    fun getEnabledPackages(): Set<String> = getAppFilters().filter { it.isEnabled }.map { it.packageName }.toSet()

    fun getPriorityContacts(): Set<String> {
        val json = p.getString("priority", "[]") ?: "[]"
        return try { val arr = JSONArray(json); (0 until arr.length()).map { arr.getString(it) }.toSet() } catch (e: Exception) { emptySet() }
    }

    fun savePriorityContacts(contacts: Set<String>) {
        val arr = JSONArray(); contacts.forEach { arr.put(it) }
        p.edit().putString("priority", arr.toString()).apply()
    }

    fun isPriorityContact(name: String) = getPriorityContacts().contains(name)
}
