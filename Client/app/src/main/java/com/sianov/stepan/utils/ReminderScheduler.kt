package com.sianov.stepan.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object ReminderScheduler {

    private const val TAG = "ReminderScheduler"

    fun scheduleReminders(context: Context, performanceUrl: String, title: String, dateStr: String) {
        val performanceTime = parseDate(dateStr) ?: return
        val currentTime = System.currentTimeMillis()

        if (performanceTime <= currentTime) return

        // 1. За 1 день
        scheduleAlarm(context, performanceUrl, title, performanceTime - 24 * 60 * 60 * 1000, 1, "Завтра спектакль: $title", 100)
        
        // 2. За 12 часов
        scheduleAlarm(context, performanceUrl, title, performanceTime - 12 * 60 * 60 * 1000, 2, "Спектакль сегодня: $title", 200)
        
        // 3. За 3 часа
        scheduleAlarm(context, performanceUrl, title, performanceTime - 3 * 60 * 60 * 1000, 3, "Скоро начало: $title", 300)
    }

    fun cancelReminders(context: Context, performanceUrl: String) {
        cancelAlarm(context, performanceUrl, 1, 100)
        cancelAlarm(context, performanceUrl, 2, 200)
        cancelAlarm(context, performanceUrl, 3, 300)
    }

    private fun scheduleAlarm(
        context: Context,
        url: String,
        title: String,
        triggerTime: Long,
        type: Int,
        message: String,
        baseId: Int
    ) {
        if (triggerTime <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms. Permission missing.")
                return
            }
        }

        val notificationId = url.hashCode() + baseId
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", "Напоминание")
            putExtra("message", message)
            putExtra("notificationId", notificationId)
            putExtra("url", url)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
            Log.d(TAG, "Scheduled alarm for $title at ${Date(triggerTime)}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm", e)
        }
    }

    private fun cancelAlarm(context: Context, url: String, type: Int, baseId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationId = url.hashCode() + baseId
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun parseDate(dateStr: String): Long? {
        // Формат: "27 мая, 18:30" или "27 мая"
        try {
            val months = mapOf(
                "января" to 0, "февраля" to 1, "марта" to 2, "апреля" to 3, "мая" to 4, "июня" to 5,
                "июля" to 6, "августа" to 7, "сентября" to 8, "октября" to 9, "ноября" to 10, "декабря" to 11
            )

            val parts = dateStr.lowercase().split(" ", ",", ":").filter { it.isNotBlank() }
            if (parts.size < 2) return null

            val day = parts[0].toInt()
            val monthStr = parts[1]
            val month = months[monthStr] ?: return null
            
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            calendar.set(Calendar.YEAR, currentYear)
            
            // Если месяц уже прошел в этом году, возможно это следующий год (например, в декабре смотрим на январь)
            if (calendar.timeInMillis < System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) {
                calendar.add(Calendar.YEAR, 1)
            }

            if (parts.size >= 4) {
                val hour = parts[2].toInt()
                val minute = parts[3].toInt()
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 18) // Дефолтное время
                calendar.set(Calendar.MINUTE, 30)
            }

            return calendar.timeInMillis
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date: $dateStr", e)
            return null
        }
    }
}
