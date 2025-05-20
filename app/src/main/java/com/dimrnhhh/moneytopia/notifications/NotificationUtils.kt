package com.dimrnhhh.moneytopia.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dimrnhhh.moneytopia.MainActivity
import com.dimrnhhh.moneytopia.R
import com.dimrnhhh.moneytopia.utils.Constants.DAILY_EXPENSE_NOTIFICATION
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationUtils {
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

    fun showNotification(
        context: Context,
        displayText: String,
        notificationId: Int,
        requestCode: Int,
    ) {
        val channelId = "daily_notification_channel"

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(
                "notificationIntentData",
                NotificationIntentData(
                    context = DAILY_EXPENSE_NOTIFICATION,
                    contextId = requestCode.toString()
                )
            )
        }

        // Create a pending intent
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("Daily Expense Report")
            .setContentText(displayText)  // Show Prompt here
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(notificationId, builder.build())
        }
    }
}