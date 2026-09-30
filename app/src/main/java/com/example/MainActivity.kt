package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.example.service.UsbTetherAccessibilityService
import com.example.util.PreferencesManager
import com.example.util.TetherHelper

class MainActivity : Activity() {

    private lateinit var tvServiceBadge: TextView
    private lateinit var tvUsbStatus: TextView
    private lateinit var ivUsbStatus: ImageView
    private lateinit var tvServiceDesc: TextView
    private lateinit var btnGrantPermission: Button
    private lateinit var switchAutoTether: Switch
    private lateinit var switchAutoReturn: Switch
    private lateinit var switchToast: Switch
    private lateinit var btnTestTether: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        tvServiceBadge = findViewById(R.id.tv_service_badge)
        tvUsbStatus = findViewById(R.id.tv_usb_status)
        ivUsbStatus = findViewById(R.id.iv_usb_status)
        tvServiceDesc = findViewById(R.id.tv_service_desc)
        btnGrantPermission = findViewById(R.id.btn_grant_permission)
        switchAutoTether = findViewById(R.id.switch_auto_tether)
        switchAutoReturn = findViewById(R.id.switch_auto_return)
        switchToast = findViewById(R.id.switch_toast)
        btnTestTether = findViewById(R.id.btn_test_tether)

        // Load saved preferences
        switchAutoTether.isChecked = PreferencesManager.isAutoTetherEnabled(this)
        switchAutoReturn.isChecked = PreferencesManager.isAutoReturnEnabled(this)
        switchToast.isChecked = PreferencesManager.isShowToastEnabled(this)
    }

    private fun setupListeners() {
        btnGrantPermission.setOnClickListener {
            openAccessibilitySettings()
        }

        btnTestTether.setOnClickListener {
            val opened = TetherHelper.openTetherSettings(this, force = true)
            if (!opened) {
                Toast.makeText(this, "Could not open Tethering Settings", Toast.LENGTH_SHORT).show()
            }
        }

        switchAutoTether.setOnCheckedChangeListener { _, isChecked ->
            PreferencesManager.setAutoTetherEnabled(this, isChecked)
        }

        switchAutoReturn.setOnCheckedChangeListener { _, isChecked ->
            PreferencesManager.setAutoReturnEnabled(this, isChecked)
        }

        switchToast.setOnCheckedChangeListener { _, isChecked ->
            PreferencesManager.setShowToastEnabled(this, isChecked)
        }
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }

    private fun updateServiceStatus() {
        val isServiceActive = isAccessibilityServiceEnabled(this)

        if (isServiceActive) {
            tvServiceBadge.text = getString(R.string.status_service_active)
            tvServiceBadge.setBackgroundResource(R.drawable.bg_badge_active)
            tvServiceBadge.setTextColor(getColor(R.color.status_green_text))
            tvServiceDesc.text = getString(R.string.desc_service_active)
            btnGrantPermission.text = getString(R.string.btn_permission_granted)
            btnGrantPermission.setBackgroundResource(R.drawable.bg_button_secondary)
            btnGrantPermission.setTextColor(getColor(R.color.status_green_text))
        } else {
            tvServiceBadge.text = getString(R.string.status_service_inactive)
            tvServiceBadge.setBackgroundResource(R.drawable.bg_badge_inactive)
            tvServiceBadge.setTextColor(getColor(R.color.status_amber_text))
            tvServiceDesc.text = getString(R.string.desc_service_inactive)
            btnGrantPermission.text = getString(R.string.btn_grant_permission)
            btnGrantPermission.setBackgroundResource(R.drawable.bg_button_primary)
            btnGrantPermission.setTextColor(getColor(R.color.text_white))
        }

        val isUsbConnected = UsbTetherAccessibilityService.isUsbConnected
        if (isUsbConnected) {
            tvUsbStatus.text = getString(R.string.status_usb_connected)
            tvUsbStatus.setTextColor(getColor(R.color.status_green_text))
            ivUsbStatus.setColorFilter(getColor(R.color.status_green_text))
        } else {
            tvUsbStatus.text = getString(R.string.status_usb_disconnected)
            tvUsbStatus.setTextColor(getColor(R.color.text_secondary))
            ivUsbStatus.setColorFilter(getColor(R.color.text_secondary))
        }
    }

    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            Toast.makeText(this, "Find 'USB Auto Tether' and turn it ON", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open Accessibility Settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        if (UsbTetherAccessibilityService.isRunning) return true

        val expectedComponent = "${context.packageName}/${UsbTetherAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(expectedComponent, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
