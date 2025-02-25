package com.dimrnhhh.moneytopia.smsHandling

import android.content.ContentResolver
import android.content.Context
import android.provider.Telephony
import com.dimrnhhh.moneytopia.models.SmsData

fun ingestSmsData(context: Context) {
    val contentResolver: ContentResolver = context.contentResolver
    val cursor = contentResolver.query(
        Telephony.Sms.CONTENT_URI,
        null,
        null,
        null,
        null
    )

    val smsList = mutableListOf<SmsData>()
    if(cursor != null && cursor.moveToFirst()) {
        do {
            val id = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms._ID))
            val address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
            val body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))
            val dateSent = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT))

            smsList.add(
                SmsData(
                    id = id,
                    address = address,
                    body = body,
                    date = date,
                    dateSent = dateSent
                )
            )
        } while (cursor.moveToNext())
    }
}