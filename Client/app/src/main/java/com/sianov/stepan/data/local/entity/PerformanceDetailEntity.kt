package com.sianov.stepan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sianov.stepan.data.model.CastMember
import com.sianov.stepan.data.model.PerformanceDetail

@Entity(tableName = "performance_details")
data class PerformanceDetailEntity(
    @PrimaryKey val url: String,
    val title: String,
    val imageUrl: String,
    val description: String,
    val cast: List<CastMember>, // Storing as a List might require a TypeConverter if Room doesn't handle it directly
    val galleryImages: List<String>,
    val author: String?,
    val acts: String?,
    val duration: String?
)

fun PerformanceDetailEntity.toPerformanceDetail(): PerformanceDetail {
    return PerformanceDetail(
        title = title,
        imageUrl = imageUrl,
        description = description,
        cast = cast,
        detailUrl = url,
        galleryImages = galleryImages,
        author = author,
        acts = acts,
        duration = duration
    )
}
