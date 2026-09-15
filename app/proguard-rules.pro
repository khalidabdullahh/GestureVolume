# Keep data models and annotated classes
-keepclassmembers class * {
    @androidx.annotation.Keep <fields>;
    @androidx.annotation.Keep <methods>;
}

# Keep Services
-keep class com.gesturevolume.app.service.** { *; }

# Keep Domain models and repositories
-keep class com.gesturevolume.app.domain.** { *; }
-keep class com.gesturevolume.app.data.** { *; }
-keep class com.gesturevolume.app.ui.** { *; }

# Coroutines and Flow
-keepclassmembernames class kotlinx.coroutines.** { *; }

