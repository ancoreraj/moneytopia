package com.dimrnhhh.moneytopia.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dimrnhhh.moneytopia.MainActivity
import com.dimrnhhh.moneytopia.R
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val requestCode = intent.getIntExtra("requestCode", 0)

        val permissionManager = PermissionManager(context)

        if (permissionManager.hasNotificationPermission()) {

        } else {
            Toast.makeText(context, "Notification Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(
        context: Context,
        expiryPrompt: String,
        notificationId: Int,
        requestCode: Int,
        couponId: String?
    ) {
        val channelId = "daily_notification_channel"

        // Create an intent to open MainActivity when the notification is clicked
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(
                "notificationIntentData",
                NotificationIntentData(
                    context = "REMINDER_NOTIFICATION",
                    contextId = couponId
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
            .setContentTitle("Looking for some discounts?")
            .setContentText(expiryPrompt)  // Show Prompt here
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun rescheduleForNextDay(context: Context, intent: Intent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val requestCode = intent.getIntExtra("requestCode", 0)
        val hours = intent.getIntExtra("hours", 0)
        val minutes = intent.getIntExtra("minutes", 0)

        println("620555 NotificationReceiver Rescheduling for next day at $hours:$minutes,$requestCode")
        // Set the calendar for the next day at the same time
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)  // Move to the next day
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
        }

        val rescheduleIntent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("hours", hours)
            putExtra("minutes", minutes)
            putExtra("requestCode", requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            /* context = */ context,
            /* requestCode = */ requestCode,
            /* intent = */ rescheduleIntent,
            /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the exact time for the next day
        alarmManager.setExactAndAllowWhileIdle(
            /* type = */ AlarmManager.RTC_WAKEUP,
            /* triggerAtMillis = */ calendar.timeInMillis,
            /* operation = */ pendingIntent
        )
    }
}