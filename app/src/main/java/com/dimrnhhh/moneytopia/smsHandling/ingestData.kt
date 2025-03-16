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

fun ingestSmsData(context: Context) {
    val contentResolver: ContentResolver = context.contentResolver
    val cursor = contentResolver.query(
        Telephony.Sms.CONTENT_URI,
        null,
        null,
        null,
        null
    )

    if(cursor != null && cursor.moveToFirst()) {
        var count = 0

        do {
            count++;
            if (count >= 1000) {
                break;
            }
            val id = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms._ID))
            val address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
            val body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))
            val dateSent = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT))

            CoroutineScope(Dispatchers.IO).launch {
            saveData(
                SmsData(
                    id = id,
                    address = address,
                    body = body,
                    date = date,
                    dateSent = dateSent
                )
            )}

        } while (cursor.moveToNext())
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