package com.sianov.stepan.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

object DateUtils {
    private val months = mapOf(
        "января" to 1, "январь" to 1,
        "февраля" to 2, "февраль" to 2,
        "марта" to 3, "март" to 3,
        "апреля" to 4, "апрель" to 4,
        "мая" to 5, "май" to 5,
        "июня" to 6, "июнь" to 6,
        "июля" to 7, "июль" to 7,
        "августа" to 8, "август" to 8,
        "сентября" to 9, "сентябрь" to 9,
        "октября" to 10, "октябрь" to 10,
        "ноября" to 11, "ноябрь" to 11,
        "декабря" to 12, "декабрь" to 12
    )

    /**
     * Parses date like "12 июня, пятница 18:30" or "12 июня"
     */
    fun parsePerformanceDate(dateStr: String): LocalDateTime? {
        try {
            val parts = dateStr.lowercase().split(Regex("[\\s\\u00A0\\u2007\\u202F]+")).filter { it.isNotEmpty() }
            if (parts.size < 2) return null

            val day = parts[0].toIntOrNull() ?: return null
            val monthName = parts[1].replace(",", "").trim()
            val month = months[monthName] ?: return null
            
            val now = LocalDateTime.now()
            var year = now.year
            
            // Если сейчас декабрь, а спектакль в январе — это скорее всего следующий год
            if (now.monthValue == 12 && month == 1) {
                year++
            }
            // Если сейчас январь, а спектакль в декабре — это скорее всего прошлый год (для кэша)
            else if (now.monthValue == 1 && month == 12) {
                year--
            }

            var hour = 18
            var minute = 30
            
            // Пытаемся найти время (обычно в конце строки, напр. "18:30")
            val timePart = parts.last()
            if (timePart.contains(":")) {
                val timeParts = timePart.split(":")
                hour = timeParts[0].toIntOrNull() ?: 18
                minute = timeParts[1].toIntOrNull() ?: 30
            }

            return LocalDateTime.of(year, month, day, hour, minute)
        } catch (e: Exception) {
            return null
        }
    }

    fun isWithinLastThreeDays(dateStr: String): Boolean {
        val performanceDate = parsePerformanceDate(dateStr) ?: return false
        val now = LocalDateTime.now()
        
        // Спектакль в прошлом, но не более 3 дней назад
        return performanceDate.isBefore(now) && performanceDate.isAfter(now.minusDays(3))
    }

    fun isPast(dateStr: String): Boolean {
        val performanceDate = parsePerformanceDate(dateStr) ?: return false
        return performanceDate.isBefore(LocalDateTime.now())
    }
}
