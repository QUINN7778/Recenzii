package com.sianov.stepan.data.model

data class ReviewRequest(
    val url: String,
    val rating: Int,
    val comment: String
)
