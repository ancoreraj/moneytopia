package com.dimrnhhh.moneytopia.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dimrnhhh.moneytopia.smsHandling.getSumOfExpensesInLastNHours
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {

        println("620555 Worker Active")
        val requestCode = inputData.getInt("requestCode", 0)

        val permissionManager = PermissionManager(appContext)
        if (!permissionManager.hasNotificationPermission()) return Result.failure()

        CoroutineScope(Dispatchers.Main).launch {
            val sumOfExpenses = getSumOfExpensesInLastNHours(24)
            showNotification(
                context = appContext,
                displayText = "Your expenses in the last 24 hours is ₹ $sumOfExpenses",
                notificationId = requestCode,
                requestCode = requestCode
            )
        }
        return Result.success()
    }
}