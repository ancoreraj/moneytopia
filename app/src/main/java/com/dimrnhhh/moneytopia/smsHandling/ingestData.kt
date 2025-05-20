package com.dimrnhhh.moneytopia.smsHandling

import android.content.ContentResolver
import android.content.Context
import android.provider.Telephony
import android.util.Log
import com.dimrnhhh.moneytopia.database.realm
import com.dimrnhhh.moneytopia.models.Expense
import com.dimrnhhh.moneytopia.models.ExpenseSource
import com.dimrnhhh.moneytopia.models.Recurrence
import com.dimrnhhh.moneytopia.models.SmsData
import com.dimrnhhh.moneytopia.utils.calculateDateRange
import com.dimrnhhh.moneytopia.utils.epochToLocalDateTime
import io.realm.kotlin.ext.query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun ingestSmsData(context: Context) {
    val contentResolver: ContentResolver = context.contentResolver

    val threshold = getSmsThresholdStartDateInMillis()
    val selection = "${Telephony.Sms.DATE} >= ?"
    val selectionArgs = arrayOf(threshold.toString())
    val sortOrder = "${Telephony.Sms.DATE} DESC"

    val cursor = contentResolver.query(
        /* uri = */ Telephony.Sms.CONTENT_URI,
        /* projection = */ null,
        /* selection = */ selection,
        /* selectionArgs = */ selectionArgs,
        /* sortOrder = */ sortOrder
    )

    cursor?.use {
        if (it.moveToFirst()) {
            do {
                val id = it.getString(it.getColumnIndexOrThrow(Telephony.Sms._ID))
                val address = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                val body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))
                val date = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.DATE))
                val dateSent = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT))

                CoroutineScope(Dispatchers.IO).launch {
                    saveData(
                        SmsData(
                            id = id,
                            address = address,
                            body = body,
                            date = date,
                            dateSent = dateSent
                        )
                    )
                }
            } while (it.moveToNext())
        }
    }
}

suspend fun saveData(smsData: SmsData) {
    try {
        val transactionInfo = getTransactionInfo(smsData.body);
        val timeOfTransaction = epochToLocalDateTime(smsData.date);

        if (smsData.body.isEmpty() ||
            transactionInfo.account.type == null ||
            transactionInfo.transaction.type == null ||
            transactionInfo.transaction.amount == null ||
            timeOfTransaction == null ||
            transactionInfo.transaction.type == "credit"
        ) {
            return
        }
        val expense = Expense(
            amount = transactionInfo.transaction.amount.toDouble(),
            category = smsData.address,
            date = timeOfTransaction,
            recurrence = Recurrence.None,
            note = "SMS_DATA",
            smsId = smsData.id,
            source = ExpenseSource.SMS
        )

        if (doesSmsExist(smsId = smsData.id)) return

        realm.write {
            this.copyToRealm(expense)
        }
    } catch (e: Exception) {
        Log.d("Exception", e.message.toString())
    }

}

fun getSmsThresholdStartDateInMillis(): Long {
    val calendar = Calendar.getInstance()
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val monthsToSubtract = if (dayOfMonth > 15) 2 else 3
    calendar.add(Calendar.MONTH, -monthsToSubtract)

    // Set to first day of that month
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    return calendar.timeInMillis
}

suspend fun getSumOfExpensesInLastNHours(hours: Int): String {
    var expenses = emptyList<Expense>()
    withContext(Dispatchers.IO) {
        val now = LocalDateTime.now()
        val startTime = now.minusHours(hours.toLong())

        expenses = realm.query<Expense>().find().filter {
            it.date.isAfter(startTime) &&
                    (it.date.isEqual(now) || it.date.isBefore(now))
        }
    }
    val total = expenses.sumOf { it.amount }

    return String.format(Locale.ENGLISH, "%.2f", total)
}

fun doesSmsExist(smsId: String): Boolean {
    val ans = realm.query<Expense>("smsId == $0", smsId)
        .find()
    return ans.isNotEmpty()
}

fun formattedThreshold(threshold: Long): String {
    val thresholdDateTime = Date(threshold)
    val formatter = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
    val formattedThreshold = formatter.format(thresholdDateTime)
    return formattedThreshold
}