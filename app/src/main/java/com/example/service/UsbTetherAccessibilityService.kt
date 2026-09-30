package com.example.service

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.example.R
import com.example.util.PreferencesManager
import com.example.util.TetherHelper
import com.example.util.TetherSession

class UsbTetherAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var isUsbReceiverRegistered = false
    private var lastToggleTime = 0L
    private var lastUsbConnectedState = false
    private var pendingTetherOnUnlock = false

    companion object {
        @Volatile
        var isRunning = false
            private set

        @Volatile
        var isUsbConnected = false
            private set

        var instance: UsbTetherAccessibilityService? = null
            private set
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "android.hardware.usb.action.USB_STATE" -> {
                    val connected = intent.getBooleanExtra("connected", false)
                    val rndis = intent.getBooleanExtra("rndis", false)

                    if (!connected) {
                        isUsbConnected = false
                        lastUsbConnectedState = false
                        pendingTetherOnUnlock = false
                        TetherSession.onUsbDisconnected()
                        return
                    }

                    // Connected is true
                    isUsbConnected = true

                    // If RNDIS / Tethering is ALREADY active, do not open settings
                    if (rndis) {
                        TetherSession.markTetheringEnabled()
                        return
                    }

                    if (!lastUsbConnectedState) {
                        lastUsbConnectedState = true

                        if (PreferencesManager.isAutoTetherEnabled(this@UsbTetherAccessibilityService)) {
                            wakeScreenIfOff()
                            if (isDeviceLocked()) {
                                pendingTetherOnUnlock = true
                            } else {
                                pendingTetherOnUnlock = false
                                handler.postDelayed({
                                    TetherHelper.openTetherSettings(this@UsbTetherAccessibilityService)
                                }, 250)
                            }
                        }
                    }
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    wakeScreenIfOff()
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    isUsbConnected = false
                    lastUsbConnectedState = false
                    pendingTetherOnUnlock = false
                    TetherSession.onUsbDisconnected()
                }
                Intent.ACTION_USER_PRESENT -> {
                    if (isUsbConnected && (pendingTetherOnUnlock || !TetherSession.isTetheringActive)) {
                        pendingTetherOnUnlock = false
                        handler.postDelayed({
                            TetherHelper.openTetherSettings(this@UsbTetherAccessibilityService)
                        }, 250)
                    }
                }
            }
        }
    }

    private fun wakeScreenIfOff() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (pm != null && !pm.isInteractive) {
                @Suppress("DEPRECATION")
                val wakeLock = pm.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "UsbAutoTether:WakeLock"
                )
                wakeLock.acquire(3000L)
            }
        } catch (ignored: Exception) {}
    }

    private fun isDeviceLocked(): Boolean {
        val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return km?.isKeyguardLocked ?: false
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        instance = this
        registerUsbReceiver()
    }

    private fun registerUsbReceiver() {
        if (!isUsbReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction("android.hardware.usb.action.USB_STATE")
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            try {
                registerReceiver(usbReceiver, filter)
                isUsbReceiverRegistered = true
            } catch (ignored: Exception) {}
        }
    }

    private fun unregisterUsbReceiver() {
        if (isUsbReceiverRegistered) {
            try {
                unregisterReceiver(usbReceiver)
                isUsbReceiverRegistered = false
            } catch (ignored: Exception) {}
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!PreferencesManager.isAutoTetherEnabled(this)) return

        val now = System.currentTimeMillis()
        if (now - lastToggleTime < 5000L) {
            return
        }

        val eventType = event.eventType
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {

            val rootNode = rootInActiveWindow ?: return
            findAndToggleUsbTether(rootNode)
        }
    }

    private fun findAndToggleUsbTether(root: AccessibilityNodeInfo) {
        val candidates = mutableListOf<AccessibilityNodeInfo>()
        collectCandidateNodes(root, candidates)

        for (node in candidates) {
            if (tryToggleNode(node)) {
                lastToggleTime = System.currentTimeMillis()
                TetherSession.markTetheringEnabled()
                onTetheringToggled()
                break
            }
        }
    }

    private fun collectCandidateNodes(node: AccessibilityNodeInfo?, list: MutableList<AccessibilityNodeInfo>) {
        if (node == null) return

        val text = node.text?.toString()?.lowercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
        val viewId = node.viewIdResourceName?.lowercase() ?: ""

        val isMatch = (text.contains("usb") && (text.contains("tether") || text.contains("partage") || text.contains("compartir") || text.contains("modem"))) ||
                (contentDesc.contains("usb") && contentDesc.contains("tether")) ||
                viewId.contains("usb_tether")

        if (isMatch) {
            list.add(node)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            collectCandidateNodes(child, list)
        }
    }

    @Suppress("DEPRECATION")
    private fun tryToggleNode(node: AccessibilityNodeInfo): Boolean {
        if (node.isCheckable) {
            if (!node.isChecked) {
                return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            } else {
                return true
            }
        }

        val parent = node.parent
        if (parent != null) {
            val switchNode = findCheckableChild(parent)
            if (switchNode != null) {
                if (!switchNode.isChecked) {
                    val success = switchNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (!success) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                    return true
                } else {
                    return true
                }
            }

            if (parent.isClickable) {
                return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }

        return false
    }

    private fun findCheckableChild(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (child.isCheckable) {
                return child
            }
            val nested = findCheckableChild(child)
            if (nested != null) {
                return nested
            }
        }
        return null
    }

    private fun onTetheringToggled() {
        if (PreferencesManager.isShowToastEnabled(this)) {
            handler.post {
                Toast.makeText(this, getString(R.string.msg_tether_enabled), Toast.LENGTH_SHORT).show()
            }
        }

        // Fast background auto-dismiss so the settings screen doesn't stay visible
        if (PreferencesManager.isAutoReturnEnabled(this)) {
            handler.postDelayed({
                performGlobalAction(GLOBAL_ACTION_BACK)
            }, 120)
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        instance = null
        unregisterUsbReceiver()
    }
}
