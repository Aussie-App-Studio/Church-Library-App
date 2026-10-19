# -----------------------------------------------------------------------------
# Kotlinx Serialization Rules
# -----------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses, Signature

# Keep all classes annotated with @Serializable and their generated serializers
-keep @kotlinx.serialization.Serializable class * {
    *;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclassmembers class * {
    *** $serializer;
}

# Keep our specific data models for backups, sync, and snapshots
-keep class com.churchbooklibrary.core.backup.** { *; }
-keep class com.churchbooklibrary.core.database.** { *; }
-keep class com.churchbooklibrary.core.sync.** { *; }

# -----------------------------------------------------------------------------
# Android Room Database Rules
# -----------------------------------------------------------------------------
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# -----------------------------------------------------------------------------
# Google Play Services (Nearby Connections & Code Scanner)
# -----------------------------------------------------------------------------
-keep class com.google.android.gms.nearby.** { *; }
-keep class com.google.mlkit.vision.codescanner.** { *; }

# -----------------------------------------------------------------------------
# Coil Image Loading & OkHttp
# -----------------------------------------------------------------------------
-dontwarn coil3.**
-dontwarn okhttp3.**
-dontwarn okio.**