package com.sianov.stepan.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Спектакль"
        val message = intent.getStringExtra("message") ?: "Скоро начало!"
        val notificationId = intent.getIntExtra("notificationId", 0)

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showReminderNotification(title, message, notificationId)
    }
}
