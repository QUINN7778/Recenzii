package com.sianov.stepan.data.local.dao

import androidx.room.*
import com.sianov.stepan.data.local.entity.ImageCacheEntity

@Dao
interface ImageCacheDao {
    @Query("SELECT imageData FROM image_cache WHERE url = :url")
    suspend fun getImageBytes(url: String): ByteArray?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheImage(imageCacheEntity: ImageCacheEntity)

    @Query("DELETE FROM image_cache WHERE url = :url")
    suspend fun deleteCachedImage(url: String)
}
