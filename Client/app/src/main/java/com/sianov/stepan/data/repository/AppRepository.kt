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
    // Кэш для деталей спектаклей, чтобы не качать одно и то же по сто раз
    private val performanceCache = mutableMapOf<String, PerformanceDetail>()

    suspend fun getPosters(): List<AppItem> = scraper.fetchPosters()
    suspend fun getNews(): List<AppItem> = scraper.fetchNews()
    suspend fun getImageBytes(url: String): ByteArray? = scraper.getImageBytes(url)
    
    suspend fun getPerformanceDetail(url: String): PerformanceDetail? {
        // Если в кэше уже есть — отдаем мгновенно
        performanceCache[url]?.let { return it }
        
        // Если нет — качаем
        val detail = scraper.fetchPerformanceDetail(url)
        
        // И сохраняем в кэш
        if (detail != null) {
            performanceCache[url] = detail
        }
        return detail
    }
}
