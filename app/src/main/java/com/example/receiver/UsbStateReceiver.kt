package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.service.UsbTetherAccessibilityService
import com.example.util.PreferencesManager
import com.example.util.TetherHelper
import com.example.util.TetherSession

class UsbStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        // If the accessibility service is running, it handles USB events directly.
        // Skipping here prevents double-launching the tethering settings.
        if (UsbTetherAccessibilityService.isRunning) {
            return
        }

        when (action) {
            "android.hardware.usb.action.USB_STATE" -> {
                val connected = intent.getBooleanExtra("connected", false)
                val rndis = intent.getBooleanExtra("rndis", false)

                if (!connected) {
                    TetherSession.onUsbDisconnected()
                    return
                }

                if (rndis) {
                    TetherSession.markTetheringEnabled()
                    return
                }

                if (PreferencesManager.isAutoTetherEnabled(context)) {
                    TetherHelper.openTetherSettings(context)
                }
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                TetherSession.onUsbDisconnected()
            }
            Intent.ACTION_USER_PRESENT -> {
                if (PreferencesManager.isAutoTetherEnabled(context) && !TetherSession.isTetheringActive) {
                    TetherHelper.openTetherSettings(context)
                }
            }
        }
    }
}
