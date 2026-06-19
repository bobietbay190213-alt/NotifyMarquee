package com.notifymarquee.model

data class AppFilter(val packageName: String, val displayName: String, var isEnabled: Boolean) {
    companion object {
        fun defaultFilters() = listOf(
            AppFilter("com.facebook.orca", "Messenger", true),
            AppFilter("com.zhiliaoapp.musically", "TikTok", true),
            AppFilter("com.zing.zalo", "Zalo", true),
            AppFilter("org.telegram.messenger", "Telegram", true),
            AppFilter("com.discord", "Discord", true),
            AppFilter("com.facebook.katana", "Facebook", false),
            AppFilter("com.instagram.android", "Instagram", false),
            AppFilter("com.whatsapp", "WhatsApp", false)
        )
    }
}
