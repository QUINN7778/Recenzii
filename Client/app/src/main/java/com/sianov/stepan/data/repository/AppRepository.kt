package com.sianov.stepan.data.repository

import com.sianov.stepan.data.model.AppItem
import com.sianov.stepan.data.model.PerformanceDetail
import com.sianov.stepan.data.remote.IvMuzScraper
import com.sianov.stepan.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val scraper: IvMuzScraper,
    val apiService: ApiService
) {
    private val performanceCache = mutableMapOf<String, PerformanceDetail>()

    suspend fun getPosters(): List<AppItem> {
        return try {
            val fromApi = apiService.getPosters()
            if (fromApi.isNotEmpty()) fromApi else scraper.fetchPosters()
        } catch (e: Exception) {
            scraper.fetchPosters()
        }
    }

    suspend fun getNews(): List<AppItem> {
        return try {
            val fromApi = apiService.getNews()
            if (fromApi.isNotEmpty()) fromApi else scraper.fetchNews()
        } catch (e: Exception) {
            scraper.fetchNews()
        }
    }

    suspend fun getImageBytes(url: String): ByteArray? = scraper.getImageBytes(url)
    
    fun clearPerformanceCache() {
        performanceCache.clear()
    }
    
    suspend fun getPerformanceDetail(url: String): PerformanceDetail? {
        performanceCache[url]?.let { return it }
        val detail = scraper.fetchPerformanceDetail(url)
        if (detail != null) {
            performanceCache[url] = detail
        }
        return detail
    }
}
