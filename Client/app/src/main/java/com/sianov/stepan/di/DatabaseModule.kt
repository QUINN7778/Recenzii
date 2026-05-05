package com.sianov.stepan.di

import android.content.Context
import androidx.room.Room
import com.sianov.stepan.data.local.AppDatabase
import com.sianov.stepan.data.local.dao.AppItemDao
import com.sianov.stepan.data.local.dao.ImageCacheDao
import com.sianov.stepan.data.local.dao.PerformanceDetailDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database" // Database file name
        ).fallbackToDestructiveMigration() // For development: clear DB on version change
         .build()
    }

    @Provides
    fun provideAppItemDao(database: AppDatabase): AppItemDao {
        return database.appItemDao()
    }

    @Provides
    fun providePerformanceDetailDao(database: AppDatabase): PerformanceDetailDao {
        return database.performanceDetailDao()
    }

    @Provides
    fun provideImageCacheDao(database: AppDatabase): ImageCacheDao {
        return database.imageCacheDao()
    }
}
