package com.example.cs501clockin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cs501clockin.calendar.CalendarEventRepository
import com.example.cs501clockin.data.repo.SavedLocationRepository
import com.example.cs501clockin.data.repo.UserPreferences
import com.example.cs501clockin.data.repo.UserPreferencesRepository
import com.example.cs501clockin.data.state.ActiveSessionStore
import com.example.cs501clockin.location.LocationResult
import com.example.cs501clockin.model.Session
import com.example.cs501clockin.model.SavedLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class LocationSuggestion(
    val savedLocationId: Long,
    val label: String,
    val suggestedTag: String
)

data class CalendarSuggestion(
    val eventId: Long,
    val title: String,
    val suggestedTag: String
)

data class SuggestionsUiState(
    val locationSuggestion: LocationSuggestion? = null,
    val calendarSuggestion: CalendarSuggestion? = null
)

class SuggestionsViewModel(
    private val savedLocationRepository: SavedLocationRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val activeSessionStore: ActiveSessionStore,
    private val calendarEventRepository: CalendarEventRepository,
    locationUiState: StateFlow<LocationUiState>
) : ViewModel() {

    private val dismissKey = MutableStateFlow<Pair<Long?, Long>>(null to 0L)
    private val dismissCalendarKey = MutableStateFlow<Pair<Long?, Long>>(null to 0L)
    private val clock = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(30_000L)
        }
    }

    val uiState: StateFlow<SuggestionsUiState> = combine(
        locationUiState,
        savedLocationRepository.observeAll(),
        userPreferencesRepository.data,
        activeSessionStore.activeSession,
        dismissKey,
        dismissCalendarKey,
        clock
    ) { values ->
        val loc = values[0] as LocationUiState
        val saved = values[1] as List<SavedLocation>
        val prefs = values[2] as UserPreferences
        val active = values[3] as Session
        val dismiss = values[4] as Pair<Long?, Long>
        val calendarDismiss = values[5] as Pair<Long?, Long>
        val now = values[6] as Long

        SuggestionsUiState(
            locationSuggestion = computeSuggestion(loc, saved, prefs, active, dismiss),
            calendarSuggestion = computeCalendarSuggestion(prefs, active, calendarDismiss, now)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SuggestionsUiState()
    )

    private fun computeSuggestion(
        loc: LocationUiState,
        saved: List<SavedLocation>,
        prefs: UserPreferences,
        active: Session,
        dismiss: Pair<Long?, Long>
    ): LocationSuggestion? {
        if (!prefs.locationSuggestionsEnabled) return null
        val latLng = (loc.result as? LocationResult.Success)?.latLng ?: return null

        val nearest = savedLocationRepository.nearestWithinRadius(
            latLng.latitude,
            latLng.longitude,
            saved
        ) ?: return null

        if (nearest.suggestedTag == active.tag) return null

        val now = System.currentTimeMillis()
        val (dismissId, until) = dismiss
        if (dismissId == nearest.id && now < until) return null

        return LocationSuggestion(
            savedLocationId = nearest.id,
            label = nearest.label,
            suggestedTag = nearest.suggestedTag
        )
    }

    private fun computeCalendarSuggestion(
        prefs: UserPreferences,
        active: Session,
        dismiss: Pair<Long?, Long>,
        now: Long
    ): CalendarSuggestion? {
        if (!prefs.calendarSuggestionsEnabled) return null
        if (prefs.calendarTagRules.isEmpty()) return null

        val candidate = calendarEventRepository.findNextMatchingEvent(
            now - LOOKBACK_WINDOW_MILLIS,
            prefs.calendarTagRules
        ) ?: return null

        val inWindow = now >= candidate.startTimeMillis &&
            now <= candidate.startTimeMillis + NOTIFY_WINDOW_MILLIS
        if (!inWindow) return null
        if (candidate.matchedTag == active.tag) return null

        val (dismissId, until) = dismiss
        if (dismissId == candidate.eventId && now < until) return null

        return CalendarSuggestion(
            eventId = candidate.eventId,
            title = candidate.title,
            suggestedTag = candidate.matchedTag
        )
    }

    fun dismissCurrentSuggestion() {
        val s = uiState.value.locationSuggestion ?: return
        dismissKey.update { s.savedLocationId to (System.currentTimeMillis() + 600_000L) }
    }

    fun dismissCalendarSuggestion() {
        val s = uiState.value.calendarSuggestion ?: return
        dismissCalendarKey.update { s.eventId to (System.currentTimeMillis() + CALENDAR_DISMISS_WINDOW_MILLIS) }
    }

    private companion object {
        const val NOTIFY_WINDOW_MILLIS = 5 * 60 * 1000L
        const val LOOKBACK_WINDOW_MILLIS = 2 * 60 * 1000L
        const val CALENDAR_DISMISS_WINDOW_MILLIS = 10 * 60 * 1000L
    }
}

class SuggestionsViewModelFactory(
    private val savedLocationRepository: SavedLocationRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val activeSessionStore: ActiveSessionStore,
    private val calendarEventRepository: CalendarEventRepository,
    private val locationUiState: StateFlow<LocationUiState>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SuggestionsViewModel(
            savedLocationRepository,
            userPreferencesRepository,
            activeSessionStore,
            calendarEventRepository,
            locationUiState
        ) as T
    }
}
