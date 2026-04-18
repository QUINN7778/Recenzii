package com.sianov.stepan.data.model

data class AppItem(
    val title: String,
    val description: String,
    val date: String,
    val imageUrl: String,
    val detailUrl: String = ""
)
