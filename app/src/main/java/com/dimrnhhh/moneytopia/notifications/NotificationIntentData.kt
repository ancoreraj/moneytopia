package com.dimrnhhh.moneytopia.notifications

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class NotificationIntentData(
    val context: String? = null,
    val contextId: String? = null,
) : Parcelable
