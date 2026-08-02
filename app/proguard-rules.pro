# Preserve Kotlin metadata and reflection
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }

# Preserve AndroidX / AppCompat
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Preserve the main Activity (prevents it from being renamed/optimized out)
-keep class com.android.codestudio.app.MainActivity { *; }

# Keep native method names (if you use any .so libraries like graphics-path)
-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve Parcelable (for serialization, if used later)
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
