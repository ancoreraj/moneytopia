package com.dimrnhhh.moneytopia.smsHandling

import android.content.ContentResolver
import android.content.Context
import android.provider.Telephony
import android.util.Log
import com.dimrnhhh.moneytopia.database.realm
import com.dimrnhhh.moneytopia.models.Expense
import com.dimrnhhh.moneytopia.models.Recurrence
import com.dimrnhhh.moneytopia.models.SmsData
import com.dimrnhhh.moneytopia.utils.epochToLocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

fun ingestSmsData(context: Context) {
    val contentResolver: ContentResolver = context.contentResolver

    val threshold = getSmsThresholdInMillis()
    println("620555 threshold $threshold")
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
        if(it.moveToFirst()) {
            do {
                val id = it.getString(it.getColumnIndexOrThrow(Telephony.Sms._ID))
                val address = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                val body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))
                val date = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.DATE))
                val dateSent = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT))

                println("620555 date $date dateSent $dateSent")
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
            timeOfTransaction == null
        ) {
            return
        }

        val expense = Expense(transactionInfo.transaction.amount.toDouble(), smsData.address, timeOfTransaction, Recurrence.None, "SMS_DATA" )

        realm.write { this.copyToRealm(expense) }
    } catch (e : Exception) {
        Log.d("Exception", e.message.toString())
    }

}

fun getSmsThresholdInMillis(): Long {
    val calendar = Calendar.getInstance()
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val monthsToSubtract = if (day > 15) 2 else 3
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.add(Calendar.MONTH, -monthsToSubtract)

    return calendar.timeInMillis
}
