package com.example.cs501clockin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cs501clockin.data.repo.SessionRepository
import com.example.cs501clockin.model.Session
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val tags: List<String> = listOf("Idle", "Study", "Class", "Gym", "Work", "Errands"),
    val selectedTag: String = "Idle",
    val activeSession: Session? = null
)

class HomeViewModel(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onTagSelected(tag: String) {
        _uiState.value = _uiState.value.copy(selectedTag = tag)
    }

    fun startSession() {
        val current = _uiState.value
        if (current.selectedTag == "Idle") return
        if (current.activeSession != null) return
        _uiState.value = current.copy(
            activeSession = Session(
                id = System.currentTimeMillis(),
                tag = current.selectedTag,
                startTimeMillis = System.currentTimeMillis()
            )
        )
    }

    fun endSession(): Session? {
        val current = _uiState.value
        val active = current.activeSession ?: return null
        val completed = active.copy(endTimeMillis = System.currentTimeMillis())
        _uiState.value = current.copy(
            activeSession = null,
            selectedTag = "Idle"
        )
        viewModelScope.launch {
            sessionRepository.upsert(completed)
        }
        return completed
    }
}

class HomeViewModelFactory(
    private val sessionRepository: SessionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(sessionRepository) as T
    }
}

