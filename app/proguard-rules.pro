# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Room entities and DAOs
-keep class androidx.room.** { *; }
-keep class com.example.data.local.entity.** { *; }
-keep class com.example.data.local.dao.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# Keep data models
-keep class com.example.data.model.** { *; }

# Coil keep rules
-keep class coil.** { *; }
