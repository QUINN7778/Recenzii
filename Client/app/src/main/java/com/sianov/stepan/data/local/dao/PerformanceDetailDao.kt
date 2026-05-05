package com.sianov.stepan.data.local.dao

import androidx.room.*
import com.sianov.stepan.data.local.entity.PerformanceDetailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PerformanceDetailDao {
    @Query("SELECT * FROM performance_details WHERE url = :url")
    fun getDetailByUrl(url: String): Flow<PerformanceDetailEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetail(detail: PerformanceDetailEntity)

    @Query("DELETE FROM performance_details WHERE url = :url")
    suspend fun deleteDetailByUrl(url: String)

    @Transaction
    suspend fun updateDetail(detail: PerformanceDetailEntity) {
        deleteDetailByUrl(detail.url)
        insertDetail(detail)
    }
}
