package com.example.cs501clockin.data.repo

import com.example.cs501clockin.network.OpenMeteoApi

data class WeatherNow(
    val temperatureF: Double
)

class WeatherRepository(
    private val api: OpenMeteoApi
) {
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherNow {
        val response = api.forecast(latitude = latitude, longitude = longitude)
        val temp = response.current?.temperature2m
            ?: error("Missing temperature in API response")
        return WeatherNow(
            temperatureF = temp
        )
    }
}

