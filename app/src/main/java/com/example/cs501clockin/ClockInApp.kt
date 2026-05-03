package com.example.cs501clockin

import android.app.Application
import androidx.room.Room
import com.example.cs501clockin.data.db.AppDatabase
import com.example.cs501clockin.data.repo.SavedLocationRepository
import com.example.cs501clockin.data.repo.SessionRepository
import com.example.cs501clockin.data.repo.UserPreferencesRepository
import com.example.cs501clockin.data.state.ActiveSessionStore
import com.example.cs501clockin.location.LocationRepository
import com.example.cs501clockin.data.repo.homeScreenTagChips
import com.example.cs501clockin.notification.SessionTrackingService
import com.example.cs501clockin.widget.TagSwitchWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ClockInApp : Application() {
    lateinit var sessionRepository: SessionRepository
        private set
    lateinit var savedLocationRepository: SavedLocationRepository
        private set
    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set
    lateinit var activeSessionStore: ActiveSessionStore
        private set
    lateinit var locationRepository: LocationRepository
        private set

    private val applicationJob = SupervisorJob()
    private val applicationScope = CoroutineScope(applicationJob + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "clockin.db"
        )
            .fallbackToDestructiveMigration()
            .build()

        sessionRepository = SessionRepository(db.sessionDao())
        savedLocationRepository = SavedLocationRepository(db.savedLocationDao())
        userPreferencesRepository = UserPreferencesRepository(applicationContext)
        activeSessionStore = ActiveSessionStore(sessionRepository)
        locationRepository = LocationRepository(applicationContext)

        applicationScope.launch {
            combine(
                activeSessionStore.activeSession,
                userPreferencesRepository.data
            ) { session, prefs ->
                prefs.notificationsEnabled
            }
                .distinctUntilChanged()
                .collect { shouldRun ->
                    if (shouldRun) {
                        SessionTrackingService.start(this@ClockInApp)
                    } else {
                        SessionTrackingService.stop(this@ClockInApp)
                    }
                }
        }

        applicationScope.launch {
            combine(
                activeSessionStore.activeSession,
                userPreferencesRepository.data
            ) { session, prefs ->
                session.tag to prefs.homeScreenTagChips()
            }
                .distinctUntilChanged()
                .collect {
                    TagSwitchWidgetProvider.requestUpdateAll(this@ClockInApp)
                }
        }
    }
}

