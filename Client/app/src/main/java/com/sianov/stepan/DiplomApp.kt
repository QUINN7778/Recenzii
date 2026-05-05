package com.sianov.stepan

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import com.sianov.stepan.di.NetworkModule
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class DiplomApp : Application(), ImageLoaderFactory {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ImageLoaderEntryPoint {
        fun okHttpClient(): OkHttpClient
    }

    override fun newImageLoader(): ImageLoader {
        val okHttpClient = EntryPointAccessors.fromApplication(
            this,
            ImageLoaderEntryPoint::class.java
        ).okHttpClient()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient) // Используем наш настроенный клиент с лимитами
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.1)
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
    }
}
