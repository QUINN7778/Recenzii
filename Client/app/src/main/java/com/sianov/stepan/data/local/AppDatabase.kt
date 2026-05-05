package com.sianov.stepan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sianov.stepan.data.local.dao.AppItemDao
import com.sianov.stepan.data.local.dao.ImageCacheDao
import com.sianov.stepan.data.local.dao.PerformanceDetailDao
import com.sianov.stepan.data.local.entity.AppItemEntity
import com.sianov.stepan.data.local.entity.ImageCacheEntity
import com.sianov.stepan.data.local.entity.PerformanceDetailEntity
import com.sianov.stepan.utils.ListConverters // Assuming TypeConverters will be in utils

@Database(
    entities = [AppItemEntity::class, PerformanceDetailEntity::class, ImageCacheEntity::class],
    version = 1, // Increment version if schema changes
    exportSchema = false // Keep exportSchema as false for now to avoid potential schema file conflicts
)
@TypeConverters(ListConverters::class) // Add TypeConverters for lists if needed
abstract class AppDatabase : RoomDatabase() {
    abstract fun appItemDao(): AppItemDao
    abstract fun performanceDetailDao(): PerformanceDetailDao
    abstract fun imageCacheDao(): ImageCacheDao
}
