# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Compose classes
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep AdMob classes
-keep class com.google.android.gms.ads.** { *; }

# Keep Media3 classes
-keep class androidx.media3.** { *; }

# Keep Coil classes
-keep class coil.** { *; }

# Keep navigation classes
-keep class androidx.navigation.** { *; }

# Keep ViewModel classes
-keep class androidx.lifecycle.** { *; }

# Keep Accompanist classes
-keep class com.google.accompanist.** { *; }

# Keep your app's main classes
-keep class com.sjocol.guardaestados.** { *; }

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep R classes
-keep class **.R$* {
    public static <fields>;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep JavaScript interface methods
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Keep annotations
-keepattributes *Annotation*

# Keep generic signatures
-keepattributes Signature

# Keep exceptions
-keepattributes Exceptions

# Optimize
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}