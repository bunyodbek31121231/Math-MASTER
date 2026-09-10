# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.util.TableInfo$Column { *; }
-keep class androidx.room.util.TableInfo$Index { *; }
-keep class androidx.room.util.TableInfo$ForeignKey { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext$HandlerPost {
    private void run();
}

# Preserve line numbers for better crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
