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

        // Мержим новые данные в кэш (не удаляя старые — чтобы сохранить недавно прошедшие)
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

        // Загружаем всё из кэша и фильтруем
        val allCached = appItemDao.getAllItemsByTypeSync("poster")
        val kept = allCached.filter { cached ->
            val isRecentPast = DateUtils.isWithinLastThreeDays(cached.date)
            val canParse = DateUtils.parsePerformanceDate(cached.date) != null
            val isFuture = canParse && !DateUtils.isPast(cached.date)
            isRecentPast || isFuture
        }

        // Перезаписываем БД только отфильтрованными — старьё удалится надёжно
        if (kept.size < allCached.size) {
            appItemDao.updateAllByType("poster", kept)
        }

        return kept.map { it.toAppItem() }
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
            // Очищаем старые новости перед вставкой новых
            appItemDao.updateAllByType("news", entities)
        }

        val allNews = appItemDao.getAllItemsByTypeSync("news").map { it.toAppItem() }
        
        // Сортировка: самые новые первые
        return allNews.sortedByDescending { item ->
            val parts = item.date.lowercase().split(Regex("[\\s\\u00A0\\u2007\\u202F]+")).filter { it.isNotEmpty() }
            if (parts.size >= 3) {
                val day = parts[0].toIntOrNull() ?: 1
                val monthName = parts[1].trim()
                val year = parts[2].toIntOrNull() ?: 2026
                val months = mapOf(
                    "января" to 1, "февраля" to 2, "марта" to 3, "апреля" to 4,
                    "мая" to 5, "июня" to 6, "июля" to 7, "августа" to 8,
                    "сентября" to 9, "октября" to 10, "ноября" to 11, "декабря" to 12
                )
                val month = months[monthName] ?: 1
                "%04d%02d%02d".format(year, month, day) 
            } else {
                "0"
            }
        }
    }

    suspend fun getImageBytes(url: String): ByteArray? = scraper.getImageBytes(url)
    
    fun clearPerformanceCache() {
        performanceCache.clear()
        newsDetailCache.clear()
    }
    
    suspend fun getPerformanceDetail(url: String): PerformanceDetail? {
        performanceCache[url]?.let { return it }
        var detail = scraper.fetchPerformanceDetail(url)
        if (detail != null) {
            // Если у спектакля нет изображения на детальной странице, попробуем взять его из сохраненной афиши/новостей в БД
            if (detail.imageUrl.isEmpty() && detail.galleryImages.isEmpty()) {
                val cleanUrl = url.replace("https://www.ivmuz.ru", "").replace("http://www.ivmuz.ru", "")
                val dbItem = appItemDao.getAllItemsByTypeSync("poster").find { 
                    val cleanDbUrl = it.detailUrl.replace("https://www.ivmuz.ru", "").replace("http://www.ivmuz.ru", "")
                    cleanDbUrl.isNotEmpty() && (cleanDbUrl == cleanUrl || cleanDbUrl.contains(cleanUrl) || cleanUrl.contains(cleanDbUrl))
                } ?: appItemDao.getAllItemsByTypeSync("news").find {
                    val cleanDbUrl = it.detailUrl.replace("https://www.ivmuz.ru", "").replace("http://www.ivmuz.ru", "")
                    cleanDbUrl.isNotEmpty() && (cleanDbUrl == cleanUrl || cleanDbUrl.contains(cleanUrl) || cleanUrl.contains(cleanDbUrl))
                }
                if (dbItem != null && dbItem.imageUrl.isNotEmpty()) {
                    detail = detail.copy(
                        imageUrl = dbItem.imageUrl,
                        galleryImages = listOf(dbItem.imageUrl)
                    )
                }
            }
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
