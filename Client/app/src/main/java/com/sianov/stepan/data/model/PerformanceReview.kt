package com.sianov.stepan.data.model

data class PerformanceReview(
    val performanceUrl: String,
    val performanceTitle: String,
    val rating: Int, // 1-5
    val comment: String,
    val date: Long = System.currentTimeMillis()
)
