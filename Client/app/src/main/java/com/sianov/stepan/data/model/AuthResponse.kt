package com.sianov.stepan.data.model

data class AuthResponse(
    val token: String,
    val user: UserInfo
)

data class UserInfo(
    val username: String,
    val email: String
)

data class SyncResponse(
    val status: String,
    val created: Boolean
)

data class ReviewResponse(
    val username: String,
    val rating: Int,
    val comment: String,
    val date: String
)
