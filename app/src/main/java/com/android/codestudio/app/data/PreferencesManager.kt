package com.android.codestudio.app.data

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PreferencesManager {

    private const val PREFS_NAME = "codestudio_prefs"
    private const val KEY_EXTENSIONS = "extensions"
    private const val KEY_SHOW_WELCOME = "show_welcome"
    private const val KEY_DARK_THEME = "dark_theme"

    fun getExtensions(prefs: SharedPreferences): List<Extension> {
        val json = prefs.getString(KEY_EXTENSIONS, "[]")
        val type = object : TypeToken<List<Extension>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    fun saveExtensions(prefs: SharedPreferences, extensions: List<Extension>) {
        val json = Gson().toJson(extensions)
        prefs.edit().putString(KEY_EXTENSIONS, json).apply()
    }

    fun getShowWelcome(prefs: SharedPreferences): Boolean =
        prefs.getBoolean(KEY_SHOW_WELCOME, true)

    fun setShowWelcome(prefs: SharedPreferences, show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_WELCOME, show).apply()
    }

    fun getDarkTheme(prefs: SharedPreferences): Boolean =
        prefs.getBoolean(KEY_DARK_THEME, false)

    fun setDarkTheme(prefs: SharedPreferences, isDark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, isDark).apply()
    }
}
