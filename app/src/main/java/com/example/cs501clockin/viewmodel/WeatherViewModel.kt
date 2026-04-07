package com.example.cs501clockin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cs501clockin.data.repo.WeatherNow
import com.example.cs501clockin.data.repo.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WeatherUiState(
    val isLoading: Boolean = false,
    val weather: WeatherNow? = null,
    val errorMessage: String? = null
)

class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {
    private companion object {
        const val TAG = "WeatherViewModel"
    }

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun refresh(latitude: Double, longitude: Double) {
        _uiState.value = WeatherUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val now = repository.getCurrentWeather(latitude, longitude)
                _uiState.value = WeatherUiState(isLoading = false, weather = now)
            } catch (e: Exception) {
                Log.e(TAG, "Weather refresh failed", e)
                _uiState.value = WeatherUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Weather request failed"
                )
            }
        }
    }
}

class WeatherViewModelFactory(
    private val repository: WeatherRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherViewModel(repository) as T
    }
}

