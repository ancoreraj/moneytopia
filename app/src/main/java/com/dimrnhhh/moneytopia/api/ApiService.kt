package com.dimrnhhh.moneytopia.api

import com.dimrnhhh.moneytopia.models.AppUpdateInfoResponseModel
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("/api/device")
    suspend fun getAppUpdateInfo(): Response<AppUpdateInfoResponseModel>
}