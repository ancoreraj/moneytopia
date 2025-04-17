package com.dimrnhhh.moneytopia.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dimrnhhh.moneytopia.models.Recurrence
import com.dimrnhhh.moneytopia.smsHandling.getSumOfExpenses
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val requestCode = intent.getIntExtra("requestCode", 0)

        val permissionManager = PermissionManager(context)

        if (permissionManager.hasNotificationPermission()) {
            CoroutineScope(Dispatchers.Main).launch {
                val sumOfExpenses = getSumOfExpenses(Recurrence.Daily)
                showNotification(
                    context = context,
                    displayText = "Your expenses in the last 24 hours is ₹ $sumOfExpenses",
                    notificationId = requestCode,
                    requestCode = requestCode
                )
//                rescheduleForNextDay(context, intent)
            }
        }


    }

    @SuppressLint("ScheduleExactAlarm")
    private fun rescheduleForNextDay(context: Context, intent: Intent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val requestCode = intent.getIntExtra("requestCode", 0)
        val hours = intent.getIntExtra("hours", 0)
        val minutes = intent.getIntExtra("minutes", 0)

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