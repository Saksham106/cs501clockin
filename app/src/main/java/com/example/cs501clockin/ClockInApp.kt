package com.example.cs501clockin

import android.app.Application
import androidx.room.Room
import com.example.cs501clockin.data.db.AppDatabase
import com.example.cs501clockin.data.repo.SessionRepository
import com.example.cs501clockin.data.repo.WeatherRepository
import com.example.cs501clockin.location.LocationRepository
import com.example.cs501clockin.network.OpenMeteoApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ClockInApp : Application() {
    lateinit var sessionRepository: SessionRepository
        private set
    lateinit var locationRepository: LocationRepository
        private set
    lateinit var weatherRepository: WeatherRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "clockin.db"
        ).build()
        sessionRepository = SessionRepository(db.sessionDao())
        locationRepository = LocationRepository(applicationContext)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val okHttp = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val api = retrofit.create(OpenMeteoApi::class.java)
        weatherRepository = WeatherRepository(api)
    }
}

