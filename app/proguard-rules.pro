# ═══════════════════════════════════════════════════════════════════════════════
# REGARDS AU MONDE - Règles ProGuard/R8
# Optimise l'APK : ~40% plus petit, obfuscation du code
# ═══════════════════════════════════════════════════════════════════════════════

# ── Android Framework ──────────────────────────────────────────────────────────
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.service.quicksettings.TileService

# ── Kotlin ─────────────────────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ── ML Kit (on-device object detection) ────────────────────────────────────────
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_common.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_object_detection.** { *; }
-dontwarn com.google.mlkit.**

# ── CameraX ────────────────────────────────────────────────────────────────────
-keep class androidx.camera.** { *; }
-keep interface androidx.camera.** { *; }
-dontwarn androidx.camera.**

# ── Google AdMob ───────────────────────────────────────────────────────────────
-keep class com.google.android.gms.ads.** { *; }
-keep public class com.google.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# ── Enum (ObjectType, MovementDirection) ───────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Data classes ───────────────────────────────────────────────────────────────
-keep class com.example.prevcol.CameraDetector$CameraDetectionResult { *; }
-keep class com.example.prevcol.RadarOverlay$DetectedObject { *; }

# ── Supprimer les logs en release ──────────────────────────────────────────────
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ── Optimisations agressives ───────────────────────────────────────────────────
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''
