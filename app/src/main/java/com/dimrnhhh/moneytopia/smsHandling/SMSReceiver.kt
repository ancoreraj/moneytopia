package com.dimrnhhh.moneytopia.smsHandling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.dimrnhhh.moneytopia.models.SmsData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            Thread.sleep(1000)

            val currentTime = System.currentTimeMillis()
            val recentThresholdMillis = 5 * 1000 // 5 seconds window

            val selection = "${Telephony.Sms.DATE} > ?"
            val selectionArgs = arrayOf((currentTime - recentThresholdMillis).toString())
            val sortOrder = "${Telephony.Sms.DATE} DESC"

            val cursor = context.contentResolver.query(
                /* uri = */ Telephony.Sms.Inbox.CONTENT_URI,
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
    }
}