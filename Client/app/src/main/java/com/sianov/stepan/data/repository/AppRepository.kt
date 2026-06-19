package com.sianov.stepan.data.repository

import com.sianov.stepan.data.model.AppItem
import com.sianov.stepan.data.model.PerformanceDetail
import com.sianov.stepan.data.remote.IvMuzScraper
import com.sianov.stepan.data.remote.ApiService
import com.sianov.stepan.data.local.dao.AppItemDao
import com.sianov.stepan.data.local.entity.AppItemEntity
import com.sianov.stepan.data.local.entity.toAppItem
import com.sianov.stepan.utils.DateUtils
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val scraper: IvMuzScraper,
    val apiService: ApiService,
    private val appItemDao: AppItemDao
) {
    private val performanceCache = mutableMapOf<String, PerformanceDetail>()
    private val newsDetailCache = mutableMapOf<String, String>()

    suspend fun getPosters(): List<AppItem> {
        val sitePosters = try {
            val fromApi = withTimeout(5000) {
                apiService.getPosters()
            }
            if (fromApi.isNotEmpty()) fromApi else scraper.fetchPosters()
        } catch (e: Exception) {
            scraper.fetchPosters()
        }

        // Сохраняем новые данные в БД
        if (sitePosters.isNotEmpty()) {
            val entities = sitePosters.map { 
                AppItemEntity(
                    id = it.detailUrl.ifEmpty { it.title + it.date },
                    title = it.title,
                    description = it.description,
                    date = it.date,
                    imageUrl = it.imageUrl,
                    detailUrl = it.detailUrl,
                    itemType = "poster"
                )
            }
            appItemDao.insertAll(entities)
        }

        // Получаем всё из кэша
        val cachedEntities = appItemDao.getAllItemsByTypeSync("poster")
        
        // Фильтруем: оставляем те, что есть на сайте + те, что закончились не более 3 дней назад
        val result = cachedEntities.filter { cached ->
            val isOnSite = sitePosters.any { it.detailUrl == cached.detailUrl }
            val isRecentPast = DateUtils.isWithinLastThreeDays(cached.date)
            val isFuture = !DateUtils.isPast(cached.date)
            
            isOnSite || isRecentPast || isFuture
        }

        // Чистим БД от совсем старых записей
        val toDelete = cachedEntities.filter { cached -> 
            !result.any { it.id == cached.id } 
        }
        toDelete.forEach { appItemDao.deleteByUrl(it.detailUrl, "poster") }

        return result.map { it.toAppItem() }
    }

    suspend fun getNews(): List<AppItem> {
        newsDetailCache.clear() 
        val siteNews = try {
            val fromApi = withTimeout(5000) {
                apiService.getNews()
            }
            if (fromApi.isNotEmpty()) fromApi else scraper.fetchNews()
        } catch (e: Exception) {
            scraper.fetchNews()
        }

        if (siteNews.isNotEmpty()) {
            val entities = siteNews.map { 
                AppItemEntity(
                    id = it.detailUrl.ifEmpty { it.title + it.date },
                    title = it.title,
                    description = it.description,
                    date = it.date,
                    imageUrl = it.imageUrl,
                    detailUrl = it.detailUrl,
                    itemType = "news"
                )
            }
            appItemDao.insertAll(entities)
        }

        return appItemDao.getAllItemsByTypeSync("news").map { it.toAppItem() }
    }

    suspend fun getImageBytes(url: String): ByteArray? = scraper.getImageBytes(url)
    
    fun clearPerformanceCache() {
        performanceCache.clear()
        newsDetailCache.clear()
    }
    
    suspend fun getPerformanceDetail(url: String): PerformanceDetail? {
        performanceCache[url]?.let { return it }
        val detail = scraper.fetchPerformanceDetail(url)
        if (detail != null) {
            performanceCache[url] = detail
        }
        return detail
    }

    suspend fun getNewsDetail(url: String): String {
        newsDetailCache[url]?.let { return it }
        val detail = scraper.fetchNewsDetail(url)
        if (detail.isNotEmpty()) {
            newsDetailCache[url] = detail
        }
        return detail
    }
}
