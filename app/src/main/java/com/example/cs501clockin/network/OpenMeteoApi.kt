package com.example.cs501clockin.network

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m",
        @Query("temperature_unit") temperatureUnit: String = "fahrenheit"
    ): OpenMeteoForecastResponse
}

