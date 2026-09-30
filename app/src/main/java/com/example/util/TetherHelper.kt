package com.example.util

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings

object TetherHelper {

    /**
     * Opens the system tethering settings page using fallback intents.
     * Guaranteed to only open once per USB connection session unless forced.
     */
    fun openTetherSettings(context: Context, force: Boolean = false): Boolean {
        if (!force && !TetherSession.shouldLaunchSettings()) {
            return false
        }

        val intents = listOf(
            Intent("android.settings.TETHER_SETTINGS"),
            Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.TetherSettings")),
            Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$TetherSettingsActivity")),
            Intent(Settings.ACTION_WIRELESS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )

        for (intent in intents) {
            try {
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return true
                }
            } catch (ignored: Exception) {
                // Try next intent
            }
        }

        return try {
            val lastResort = Intent("android.settings.TETHER_SETTINGS").apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
            }
            context.startActivity(lastResort)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
