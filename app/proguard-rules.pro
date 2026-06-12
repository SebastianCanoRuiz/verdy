# Verdy ProGuard Rules

# Keep application class
-keep class com.verdy.VerdyApp { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.verdy.**$$serializer { *; }
-keepclassmembers class com.verdy.** {
    *** Companion;
}
-keepclasseswithmembers class com.verdy.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ZXing
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.** { *; }

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
