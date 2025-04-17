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

            val sortOrder = "${Telephony.Sms.DATE} DESC"

            val cursor = context.contentResolver.query(
                /* uri = */ Telephony.Sms.Inbox.CONTENT_URI,
                /* projection = */ null,
                /* selection = */ null,
                /* selectionArgs = */ null,
                /* sortOrder = */ sortOrder
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val id = it.getString(it.getColumnIndexOrThrow(Telephony.Sms._ID))
                        val address = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                        val body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))
                        val date = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.DATE))
                        val dateSent =
                            it.getString(it.getColumnIndexOrThrow(Telephony.Sms.DATE_SENT))

                        // check if the above entry exists
                        if (doesSmsExist(id)) break

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