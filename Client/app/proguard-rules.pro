# --- General Android & Kotlin ---
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# --- Retrofit 2 ---
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations
-keep @retrofit2.http.* interface * { *; }
-dontwarn retrofit2.**

# --- OkHttp 3 ---
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# --- Jsoup ---
-dontwarn org.jsoup.**

# --- Hilt / Dagger ---
-keep class com.google.dagger.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *
-dontwarn dagger.hilt.**

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepclassmembernames class kotlinx.coroutines.android.** {
    public <init>(android.os.Handler, java.lang.String);
}
-dontwarn kotlinx.coroutines.**

# --- DataStore / Protocol Buffers ---
-dontwarn androidx.datastore.**

# --- App Specific Models ---
# Keep our data model to prevent issues if we ever use reflection-based serialization
-keepclassmembers class com.sianov.stepan.data.model.** { *; }
-keep class com.sianov.stepan.data.model.** { *; }
