package com.dimrnhhh.moneytopia.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit

object EventScheduler {
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            val channelId = "daily_notification_channel"

            if (notificationManager.getNotificationChannel(channelId) != null) return

            val channelName = "Daily Expense Tracker"
            val description = "Channel to notify daily expenses"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                this.description = description
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleDailyNotification(context: Context) {
        val (hours, minutes) = Pair(22, 0)
        scheduleEvent(
            context = context,
            time = Pair(hours, minutes),
            requestCode = hours * 100 + minutes
        )
    }

    fun scheduleEvent(context: Context, time: Pair<Int, Int>, requestCode: Int) {
        val currentDateTime = Calendar.getInstance()
        val scheduledTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time.first)
            set(Calendar.MINUTE, time.second)
            set(Calendar.SECOND, 0)

            if (before(currentDateTime)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val delayMillis = scheduledTime.timeInMillis - currentDateTime.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "requestCode" to requestCode
                )
            )
            .addTag("daily_expense_notification_$requestCode")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_expense_notification_$requestCode",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelEvent(context: Context, requestCode: Int) {
        WorkManager.getInstance(context)
            .cancelAllWorkByTag("daily_expense_notification_$requestCode")
    }
}