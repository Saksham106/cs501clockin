package com.example.cs501clockin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cs501clockin.location.LocationRepository
import com.example.cs501clockin.location.LocationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LocationUiState(
    val result: LocationResult? = null,
    val isLoading: Boolean = false
)

class LocationViewModel(
    private val locationRepository: LocationRepository
) : ViewModel() {
    private companion object {
        const val TAG = "LocationViewModel"
    }

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    fun refresh() {
        if (_uiState.value.isLoading) return
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            runCatching {
                locationRepository.getCurrentLocation()
            }.onSuccess { result ->
                _uiState.value = LocationUiState(result = result, isLoading = false)
            }.onFailure { error ->
                Log.e(TAG, "Failed to refresh location", error)
                _uiState.value = LocationUiState(
                    result = LocationResult.Error(error.message ?: "Location failure"),
                    isLoading = false
                )
            }
        }
    }
}

class LocationViewModelFactory(
    private val locationRepository: LocationRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LocationViewModel(locationRepository) as T
    }
}

