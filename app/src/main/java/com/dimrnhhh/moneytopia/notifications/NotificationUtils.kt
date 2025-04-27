package com.dimrnhhh.moneytopia.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dimrnhhh.moneytopia.MainActivity
import com.dimrnhhh.moneytopia.R
import com.dimrnhhh.moneytopia.utils.Constants.DAILY_EXPENSE_NOTIFICATION

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
