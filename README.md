# USB Auto Tether ⚡

> **Automatic, Hands-Free USB Tethering for Non-Rooted Android Devices.**  
> Ultra-lightweight (~300 KB), zero idle battery drain, and works without root.

---

## 📌 1. What Is This App?

**USB Auto Tether** is an automated Android utility designed to eliminate the repetitive friction of manually enabling USB Tethering every time you connect your phone to a PC, Mac, or laptop.

Whenever you plug your phone into a computer via a USB cable, the app detects the hardware connection, opens the system tethering settings, flips the switch **ON** automatically, and instantly returns to your previous screen—giving you seamless internet sharing in a fraction of a second.

---

## 🛑 2. Which Problems Does It Solve?

### Problem A: Repetitive Manual Routine Every Single Time
Android intentionally shuts down USB Tethering every time the cable is disconnected. Every time you reconnect to your PC, you are forced to:
1. Unlock your phone.
2. Open **Settings**.
3. Tap **Network & Internet** (or **Connections**).
4. Tap **Hotspot & Tethering**.
5. Locate and toggle **USB Tethering**.

*If you connect your phone 5 to 10 times a day, this manual routine wastes time and disrupts your workflow.*

### Problem B: Non-Root System Restrictions
On non-rooted Android, the OS forbids third-party apps from directly toggling tethering via background code (`android.permission.TETHER_PRIVILEGED` is reserved strictly for system OEMs). Traditional automation tools require root access or complicated ADB developer shell commands.

**USB Auto Tether solves this without root** by utilizing Android's standard **Accessibility Service API** to safely simulate the user action in milliseconds.

### Problem C: Double Screens & Screen Disruption
Other tools often trigger the settings screen multiple times as USB connection states negotiate, or leave the settings screen permanently open over your active apps.

**USB Auto Tether solves this with:**
- **Smart Session Latch (`TetherSession`)**: Guarantees the settings screen is triggered **exactly once** per cable connection.
- **RNDIS Active Check**: If USB tethering is already active, the screen is never opened.
- **Fast Auto-Dismiss**: The app automatically issues a `BACK` command ~120ms after toggling, returning you straight to your current screen or launcher.

### Problem D: Phone Locked When Plugged In
When a phone is locked with a PIN or fingerprint, Android security blocks automated clicks behind the secure lock screen.
- **USB Auto Tether handles this gracefully**: It wakes up your screen so you can immediately scan your fingerprint or face, and the **exact millisecond you unlock (`ACTION_USER_PRESENT`)**, it activates tethering.

---

## 🚀 3. How to Use (Step-by-Step Setup)

Setting up the app takes less than 30 seconds and only needs to be done once.

### Step 1: Open the App
Install and launch **USB Auto Tether**. You will see the master status marked as **`PERMISSION REQUIRED`**.

### Step 2: Grant Accessibility Permission
1. Tap the primary button: **`Enable Accessibility Service`**.
2. The phone will open Android's **Accessibility Settings**.
3. Under **Downloaded Apps** (or **Installed Services**), select **`USB Auto Tether`**.
4. Turn the toggle **ON** and tap **Allow** when prompted by Android.

### Step 3: Return to the App
Go back to **USB Auto Tether**. The status badge will now show **`SERVICE ACTIVE`** (Green).

### Step 4: Plug in and Enjoy
Plug your USB cable into your computer. 
- The app will automatically toggle USB Tethering on.
- A brief confirmation message (`USB Tethering automatically enabled!`) will appear.
- The settings screen will close itself immediately, keeping your phone on your active screen.

---

## ⚙️ 4. Preferences & Settings

| Setting | Default | Description |
| :--- | :---: | :--- |
| **Auto-Toggle Tethering** | `ON` | Automatically turns on USB tethering upon cable connection. |
| **Fast Background Auto-Dismiss** | `ON` | Dismisses the tethering settings window instantly (~120ms) after switching it on, returning you to your app. |
| **Show Confirmation Toast** | `ON` | Displays a brief non-intrusive notification confirming tethering was enabled. |
| **Test Tethering Screen** | Button | Allows you to test the screen detection and auto-switch mechanism on demand. |

---

## 🔋 5. Battery, Privacy & Performance

- **0% Idle Battery Consumption**: 
  - No background polling, no continuous threads, and no background `WakeLock`. 
  - The app stays 100% asleep until Android's Linux kernel sends a hardware USB interrupt event.
  - The trigger only runs when the phone is **plugged in and actively receiving charging power**.
- **100% Offline & Private**: 
  - Zero internet permissions declared in `AndroidManifest.xml`.
  - No analytics, no tracking, and no external network calls.
- **Ultra-Small Binary Size**: 
  - Optimized with R8 code and resource shrinking.
  - APK size is **~300 KB** (less than a third of 1 MB), saving device storage and memory.

---

## 📱 6. Compatibility

- **Supported Android Versions**: Android 7.0 (Nougat, API 24) up to Android 15 & 16 (API 36).
- **Supported OEM Skins**:
  - Google Pixel (Stock AOSP)
  - Samsung One UI
  - Xiaomi / Redmi / POCO (HyperOS / MIUI)
  - OnePlus / Oppo / Realme (OxygenOS / ColorOS)
  - Motorola (My UX)
  - Nothing OS, Vivo (Funtouch OS), and others.
