package com.dimrnhhh.moneytopia.models

import kotlinx.serialization.Serializable

@Serializable
data class AppUpdateInfoResponseModel(
    val isUpdateRequired: Boolean? = null,
    val appVersion: String? = null
)