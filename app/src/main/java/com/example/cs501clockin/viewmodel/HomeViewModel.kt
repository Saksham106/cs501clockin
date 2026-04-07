package com.example.cs501clockin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cs501clockin.data.repo.SessionRepository
import com.example.cs501clockin.model.Session
import com.example.cs501clockin.model.SessionTags
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val tags: List<String> = SessionTags.defaults,
    val selectedTag: String = SessionTags.IDLE,
    val activeSession: Session? = null
)

class HomeViewModel(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private companion object {
        const val TAG = "HomeViewModel"
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onTagSelected(tag: String) {
        _uiState.update { it.copy(selectedTag = tag) }
    }

    fun startSession() {
        val current = _uiState.value
        if (current.selectedTag == SessionTags.IDLE) return
        if (current.activeSession != null) return
        val session = Session(
                id = System.currentTimeMillis(),
                tag = current.selectedTag,
                startTimeMillis = System.currentTimeMillis()
            )
        _uiState.update { it.copy(activeSession = session) }
    }

    fun endSession(): Session? {
        val current = _uiState.value
        val active = current.activeSession ?: return null
        val completed = active.copy(endTimeMillis = System.currentTimeMillis())
        _uiState.update {
            it.copy(activeSession = null, selectedTag = SessionTags.IDLE)
        }
        viewModelScope.launch {
            runCatching {
                sessionRepository.upsert(completed)
            }.onFailure { error ->
                Log.e(TAG, "Failed to persist completed session", error)
            }
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

