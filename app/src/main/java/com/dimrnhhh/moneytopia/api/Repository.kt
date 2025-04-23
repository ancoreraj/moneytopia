package com.dimrnhhh.moneytopia.api

import com.dimrnhhh.moneytopia.models.AppUpdateInfoResponseModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class Repository {
    fun getAppUpdateInfo(): Flow<Resource<AppUpdateInfoResponseModel>> = flow {
        emit(Resource.Loading())
        val response = ApiResponseHandler.handleApiCall {
            RetrofitClient.apiService.getAppUpdateInfo()
        }
        if (response is Resource.Success) {
            if (response.data != null) {
                emit(Resource.Success(response.data))
            } else {
                emit(Resource.Error("Something went wrong!"))
            }
        } else {
            emit(Resource.Error(response.message ?: "Something went wrong!"))
        }
    }
}