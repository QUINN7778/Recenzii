package com.sianov.stepan.data.remote

import com.sianov.stepan.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("/posters/")
    suspend fun getPosters(): List<AppItem>

    @GET("/news/")
    suspend fun getNews(): List<AppItem>

    // Авторизация
    @POST("/auth/register/")
    suspend fun register(@Body data: Map<String, String>): AuthResponse

    @POST("/auth/login/")
    suspend fun login(@Body data: Map<String, String>): AuthResponse

    // Синхронизация спектакля
    @POST("/sync/")
    suspend fun syncPerformance(
        @Header("Authorization") token: String,
        @Body data: Map<String, String>
    ): SyncResponse

    // Рецензии
    @GET("/reviews/")
    suspend fun getReviews(@Query("url") url: String): List<ReviewResponse>

    @POST("/reviews/")
    suspend fun addReview(
        @Header("Authorization") token: String,
        @Body request: ReviewRequest
    ): Response<ResponseBody>
 // Изменили на ResponseBody

    // Legacy Scraping
    @GET("/ticket_online/")
    suspend fun getPostersHtml(): Response<String>

    @GET("/news/")
    suspend fun getNewsHtml(): Response<String>

    @GET
    suspend fun downloadImage(@Url url: String): Response<ResponseBody>

    @GET
    suspend fun getHtml(@Url url: String): Response<String>
}
