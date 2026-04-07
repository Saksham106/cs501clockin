package com.example.cs501clockin

import android.app.Application
import androidx.room.Room
import com.example.cs501clockin.data.db.AppDatabase
import com.example.cs501clockin.data.repo.SessionRepository
import com.example.cs501clockin.location.LocationRepository

class ClockInApp : Application() {
    lateinit var sessionRepository: SessionRepository
        private set
    lateinit var locationRepository: LocationRepository
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
    }
}

