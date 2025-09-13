package com.dimrnhhh.moneytopia.api

import retrofit2.Response

object ApiResponseHandler {
    suspend fun <T> handleApiCall(apiCall: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                Resource.Success(response.body())
            } else {
                Resource.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unknown error occurred")
        }
    }
}

