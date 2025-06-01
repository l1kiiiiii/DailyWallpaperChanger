package com.example.daily.api

import retrofit2.http.GET
import retrofit2.http.Query

interface ApodApiService {
    @GET("planetary/apod")
    suspend fun getImageOfTheDay(@Query("api_key") apiKey: String): ApodResponse
}

data class ApodResponse(
    val url: String,
    val title: String,
    val date: String
)