# Richyss R8 rules

# kotlinx.serialization keeps generated serializers referenced through reflection.
-keepclassmembers class com.richyss.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.richyss.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Room generated implementations.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**
