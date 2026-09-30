package com.example.util

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {
    private const val PREFS_NAME = "usb_auto_tether_prefs"
    private const val KEY_AUTO_TETHER = "key_auto_tether"
    private const val KEY_AUTO_RETURN = "key_auto_return"
    private const val KEY_SHOW_TOAST = "key_show_toast"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAutoTetherEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_TETHER, true)
    }

    fun setAutoTetherEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_TETHER, enabled).apply()
    }

    fun isAutoReturnEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_RETURN, true)
    }

    fun setAutoReturnEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_RETURN, enabled).apply()
    }

    fun isShowToastEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SHOW_TOAST, true)
    }

    fun setShowToastEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SHOW_TOAST, enabled).apply()
    }
}
