package com.sianov.stepan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sianov.stepan.data.model.AppItem

@Entity(tableName = "app_items")
data class AppItemEntity(
    @PrimaryKey val id: String, // Unique identifier, e.g., detailUrl or a generated UUID if detailUrl is empty
    val title: String,
    val description: String,
    val date: String,
    val imageUrl: String,
    val detailUrl: String,
    val itemType: String // To differentiate between 'poster' and 'news'
)

fun AppItemEntity.toAppItem(): AppItem {
    return AppItem(title, description, date, imageUrl, detailUrl)
}
