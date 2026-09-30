package com.example.util

import android.os.SystemClock

object TetherSession {
    private var lastLaunchTimestamp = 0L
    
    @Volatile
    var isTetheringActive = false

    @Volatile
    var hasLaunchedForThisConnection = false

    /**
     * Determines whether the settings screen should be launched.
     * Prevents duplicate launches by ensuring only one launch occurs per USB plug-in session
     * with at least an 8-second cooldown.
     */
    @Synchronized
    fun shouldLaunchSettings(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (hasLaunchedForThisConnection) {
            return false
        }
        if (now - lastLaunchTimestamp < 8000L) {
            return false
        }
        lastLaunchTimestamp = now
        hasLaunchedForThisConnection = true
        return true
    }

    /**
     * Resets the session when the USB cable is unplugged.
     */
    @Synchronized
    fun onUsbDisconnected() {
        hasLaunchedForThisConnection = false
        isTetheringActive = false
    }

    @Synchronized
    fun markTetheringEnabled() {
        isTetheringActive = true
    }
}
