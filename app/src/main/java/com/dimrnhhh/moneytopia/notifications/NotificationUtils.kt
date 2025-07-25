package com.dimrnhhh.moneytopia.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar
import kotlin.random.Random

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
//            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_notification_channel"

        if (notificationManager.getNotificationChannel(channelId) != null) return

        val channelName = "Coupons Expiry Reminder Notifications"
        val description = "Channel to notify expiring coupons"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            this.description = description
        }
        notificationManager.createNotificationChannel(channel)
    }
}

// Function to schedule the daily notification
fun scheduleDailyNotification(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val (hours, minutes) = generateRandomTimeBetween9PMAnd1030PM()
    scheduleNotification(
        context = context,
        alarmManager = alarmManager,
        hours = hours,
        minutes = minutes
    )
}

@SuppressLint("ScheduleExactAlarm")
fun scheduleNotification(
    context: Context,
    alarmManager: AlarmManager,
    hours: Int,
    minutes: Int
) {
    // Derive a unique request code based on the time
    val requestCode = hours * 100 + minutes

    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("hours", hours)
        putExtra("minutes", minutes)
        putExtra("requestCode", requestCode)
    }

    println("620555 NotificationUtils Scheduling for $hours:$minutes,$requestCode")
    val pendingIntent = PendingIntent.getBroadcast(
        /* context = */ context,
        /* requestCode = */ requestCode,
        /* intent = */ intent,
        /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Set the alarm time based on parameters
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hours)
        set(Calendar.MINUTE, minutes)
        set(Calendar.SECOND, 0)

        // If the time has already passed today, schedule for the next day
        if (timeInMillis < System.currentTimeMillis()) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    // Set exact alarm to fire at the specified time
    alarmManager.setExactAndAllowWhileIdle(
        /* type = */ AlarmManager.RTC_WAKEUP,
        /* triggerAtMillis = */ calendar.timeInMillis,
        /* operation = */ pendingIntent
    )
}

fun generateRandomTimeBetween9PMAnd1030PM(): Pair<Int, Int> {
    val startMinute = 21 * 60
    val endMinute = 22 * 60 + 30

    // Generate a random minute within the range
    val randomMinute = Random.nextInt(startMinute, endMinute + 1)

    // Calculate hours and minutes
    val hours = randomMinute / 60
    val minutes = randomMinute % 60

    return Pair(hours, minutes)
}
