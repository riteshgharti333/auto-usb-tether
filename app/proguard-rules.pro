# Proguard & R8 Optimization Rules for USB Auto Tether
# Keep Android framework entry points
-keep public class com.example.MainActivity { public *; }
-keep public class com.example.service.UsbTetherAccessibilityService { public *; }
-keep public class com.example.receiver.UsbStateReceiver { public *; }

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.accessibilityservice.AccessibilityService

# Keep custom views if any
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# R8 aggressive optimization
-repackageclasses ''
-allowaccessmodification
