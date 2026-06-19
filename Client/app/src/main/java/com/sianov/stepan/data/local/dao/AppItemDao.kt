package com.sianov.stepan.data.local.dao

import androidx.room.*
import com.sianov.stepan.data.local.entity.AppItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppItemDao {
    @Query("SELECT * FROM app_items WHERE itemType = :type ORDER BY date DESC")
    fun getAllItemsByType(type: String): Flow<List<AppItemEntity>>

    @Query("SELECT * FROM app_items WHERE itemType = :type")
    suspend fun getAllItemsByTypeSync(type: String): List<AppItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AppItemEntity>)

    @Query("DELETE FROM app_items WHERE itemType = :type")
    suspend fun deleteAllByType(type: String)

    @Query("DELETE FROM app_items WHERE detailUrl = :url AND itemType = :type")
    suspend fun deleteByUrl(url: String, type: String)

    @Transaction
    suspend fun updateAllByType(type: String, newItems: List<AppItemEntity>) {
        deleteAllByType(type)
        insertAll(newItems)
    }
}
