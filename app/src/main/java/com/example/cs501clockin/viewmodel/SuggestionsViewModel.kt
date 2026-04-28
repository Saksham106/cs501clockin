package com.example.cs501clockin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cs501clockin.data.repo.SavedLocationRepository
import com.example.cs501clockin.data.repo.UserPreferences
import com.example.cs501clockin.data.repo.UserPreferencesRepository
import com.example.cs501clockin.data.state.ActiveSessionStore
import com.example.cs501clockin.location.LocationResult
import com.example.cs501clockin.model.Session
import com.example.cs501clockin.model.SavedLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class LocationSuggestion(
    val savedLocationId: Long,
    val label: String,
    val suggestedTag: String
)

data class SuggestionsUiState(
    val suggestion: LocationSuggestion? = null
)

class SuggestionsViewModel(
    private val savedLocationRepository: SavedLocationRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val activeSessionStore: ActiveSessionStore,
    locationUiState: StateFlow<LocationUiState>
) : ViewModel() {

    private val dismissKey = MutableStateFlow<Pair<Long?, Long>>(null to 0L)

    val uiState: StateFlow<SuggestionsUiState> = combine(
        locationUiState,
        savedLocationRepository.observeAll(),
        userPreferencesRepository.data,
        activeSessionStore.activeSession,
        dismissKey
    ) { loc, saved, prefs, active, dismiss ->
        SuggestionsUiState(
            suggestion = computeSuggestion(loc, saved, prefs, active, dismiss)
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

    fun dismissCurrentSuggestion() {
        val s = uiState.value.suggestion ?: return
        dismissKey.update { s.savedLocationId to (System.currentTimeMillis() + 600_000L) }
    }
}

class SuggestionsViewModelFactory(
    private val savedLocationRepository: SavedLocationRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val activeSessionStore: ActiveSessionStore,
    private val locationUiState: StateFlow<LocationUiState>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SuggestionsViewModel(
            savedLocationRepository,
            userPreferencesRepository,
            activeSessionStore,
            locationUiState
        ) as T
    }
}
