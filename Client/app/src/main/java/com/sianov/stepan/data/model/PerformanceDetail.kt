package com.sianov.stepan.data.model

data class PerformanceDetail(
    val title: String,
    val imageUrl: String,
    val description: String,
    val cast: List<CastMember>,
    val detailUrl: String = "", // Добавляем поле для URL
    val galleryImages: List<String> = emptyList(),
    val author: String? = null,
    val acts: String? = null,
    val duration: String? = null
)

data class CastMember(
    val role: String,
    val name: String,
    val imageUrl: String? = null
)
