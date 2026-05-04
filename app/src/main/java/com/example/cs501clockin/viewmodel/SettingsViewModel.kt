package com.example.cs501clockin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cs501clockin.data.repo.SavedLocationRepository
import com.example.cs501clockin.data.repo.UserPreferencesRepository
import com.example.cs501clockin.data.repo.CalendarTagRule
import com.example.cs501clockin.ui.util.PastelTagColors
import com.example.cs501clockin.location.LocationRepository
import com.example.cs501clockin.location.LocationResult
import com.example.cs501clockin.model.SavedLocation
import com.example.cs501clockin.widget.TagSwitchWidgetProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val locationSuggestionsEnabled: Boolean = true,
    val calendarSuggestionsEnabled: Boolean = false,
    val allTags: List<String> = emptyList(),
    val homeVisibleTags: Set<String> = emptySet(),
    val notificationQuickTags: Set<String> = emptySet(),
    val customTagColors: Map<String, Int> = emptyMap(),
    val calendarTagRules: List<CalendarTagRule> = emptyList(),
    val savedLocations: List<SavedLocation> = emptyList()
)

class SettingsViewModel(
    application: Application,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val savedLocationRepository: SavedLocationRepository,
    private val locationRepository: LocationRepository
) : AndroidViewModel(application) {

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.data,
        savedLocationRepository.observeAll()
    ) { prefs, saved ->
        SettingsUiState(
            notificationsEnabled = prefs.notificationsEnabled,
            locationSuggestionsEnabled = prefs.locationSuggestionsEnabled,
            calendarSuggestionsEnabled = prefs.calendarSuggestionsEnabled,
            allTags = prefs.allTags,
            homeVisibleTags = prefs.homeVisibleTags,
            notificationQuickTags = prefs.notificationQuickTags,
            customTagColors = prefs.customTagColors,
            calendarTagRules = prefs.calendarTagRules,
            savedLocations = saved
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setNotificationsEnabled(enabled)
        }
    }

    fun setLocationSuggestionsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setLocationSuggestionsEnabled(enabled)
        }
    }

    fun setCalendarSuggestionsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setCalendarSuggestionsEnabled(enabled)
        }
    }

    fun toggleNotificationQuickTag(tag: String, select: Boolean) {
        viewModelScope.launch {
            val prefs = userPreferencesRepository.data.first()
            val current = prefs.notificationQuickTags
            val next = if (select) current + tag else current - tag
            if (select && current.size >= 3) {
                _events.emit("You can show at most 3 tags on the notification (Android limit).")
                return@launch
            }
            if (!select && next.isEmpty()) {
                _events.emit("Keep at least one tag on the notification.")
                return@launch
            }
            userPreferencesRepository.setNotificationQuickTags(next)
        }
    }

    fun toggleHomeVisibleTag(tag: String, visible: Boolean) {
        viewModelScope.launch {
            val prefs = userPreferencesRepository.data.first()
            val current = prefs.homeVisibleTags
            val next = if (visible) current + tag else current - tag
            if (!visible && next.isEmpty()) {
                _events.emit("Keep at least one tag on the Home screen.")
                return@launch
            }
            userPreferencesRepository.setHomeVisibleTags(next)
        }
    }

    fun addCustomTag(tag: String, colorArgb: Int) {
        val cleaned = tag.trim()
        if (cleaned.isEmpty()) {
            viewModelScope.launch { _events.emit("Please enter a tag name.") }
            return
        }
        val safeColor =
            if (colorArgb in PastelTagColors.CHOICES_ARGB) colorArgb else PastelTagColors.CHOICES_ARGB.first()
        viewModelScope.launch {
            userPreferencesRepository.addCustomTag(cleaned, safeColor)
            // Auto-show on home if it was previously empty selection edge case.
            val prefs = userPreferencesRepository.data.first()
            if (cleaned !in prefs.homeVisibleTags) {
                userPreferencesRepository.setHomeVisibleTags(prefs.homeVisibleTags + cleaned)
            }
            TagSwitchWidgetProvider.requestUpdateAll(getApplication())
            _events.emit("Added tag.")
        }
    }

    fun deleteCustomTag(tag: String) {
        viewModelScope.launch {
            userPreferencesRepository.deleteCustomTag(tag)
            TagSwitchWidgetProvider.requestUpdateAll(getApplication())
            _events.emit("Deleted tag.")
        }
    }

    fun addCalendarTagRule(keyword: String, tag: String) {
        val cleaned = keyword.trim()
        if (cleaned.isBlank()) {
            viewModelScope.launch { _events.emit("Please enter a keyword.") }
            return
        }
        viewModelScope.launch {
            userPreferencesRepository.addCalendarTagRule(cleaned, tag)
            _events.emit("Added calendar rule.")
        }
    }

    fun deleteCalendarTagRule(keyword: String, tag: String) {
        viewModelScope.launch {
            userPreferencesRepository.deleteCalendarTagRule(keyword, tag)
            _events.emit("Deleted calendar rule.")
        }
    }

    fun deleteSavedLocation(id: Long) {
        viewModelScope.launch {
            runCatching { savedLocationRepository.deleteById(id) }
                .onFailure { _events.emit("Could not delete location.") }
        }
    }

    fun addSavedLocationFromCurrent(label: String, suggestedTag: String, radiusMeters: Int) {
        if (label.isBlank()) {
            viewModelScope.launch { _events.emit("Please enter a label.") }
            return
        }
        val radius = radiusMeters.coerceIn(25, 2000)
        viewModelScope.launch {
            when (val loc = locationRepository.getCurrentLocation()) {
                is LocationResult.PermissionDenied ->
                    _events.emit("Location permission denied.")
                is LocationResult.Error ->
                    _events.emit(loc.message)
                is LocationResult.Success -> {
                    runCatching {
                        savedLocationRepository.insert(
                            label = label.trim(),
                            latitude = loc.latLng.latitude,
                            longitude = loc.latLng.longitude,
                            suggestedTag = suggestedTag,
                            radiusMeters = radius
                        )
                    }.onSuccess {
                        _events.emit("Saved location.")
                    }.onFailure {
                        _events.emit("Could not save location.")
                    }
                }
            }
        }
    }

    fun addSavedLocationManual(
        label: String,
        suggestedTag: String,
        radiusMeters: Int,
        latitude: Double,
        longitude: Double
    ) {
        if (label.isBlank()) {
            viewModelScope.launch { _events.emit("Please enter a label.") }
            return
        }
        val radius = radiusMeters.coerceIn(25, 2000)
        viewModelScope.launch {
            runCatching {
                savedLocationRepository.insert(
                    label = label.trim(),
                    latitude = latitude,
                    longitude = longitude,
                    suggestedTag = suggestedTag,
                    radiusMeters = radius
                )
            }.onSuccess {
                _events.emit("Saved location.")
            }.onFailure {
                _events.emit("Could not save location.")
            }
        }
    }
}

class SettingsViewModelFactory(
    private val application: Application,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val savedLocationRepository: SavedLocationRepository,
    private val locationRepository: LocationRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(
            application,
            userPreferencesRepository,
            savedLocationRepository,
            locationRepository
        ) as T
    }
}
