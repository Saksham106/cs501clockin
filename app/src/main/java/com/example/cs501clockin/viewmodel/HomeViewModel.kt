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
    val activeSession: Session = Session(
        id = System.currentTimeMillis(),
        tag = SessionTags.IDLE,
        startTimeMillis = System.currentTimeMillis()
    )
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
        switchToTag(current.selectedTag)
    }

    fun endSession(): Session? {
        val current = _uiState.value
        if (current.activeSession.tag == SessionTags.IDLE) return null

        val now = System.currentTimeMillis()
        val active = current.activeSession
        val completed = active.copy(endTimeMillis = System.currentTimeMillis())
        val idleSession = Session(
            id = now + 1,
            tag = SessionTags.IDLE,
            startTimeMillis = now
        )

        _uiState.update {
            it.copy(activeSession = idleSession, selectedTag = SessionTags.IDLE)
        }

        persistCompleted(completed)
        return completed
    }

    private fun switchToTag(nextTag: String) {
        val current = _uiState.value
        if (nextTag == current.activeSession.tag) return

        val now = System.currentTimeMillis()
        val completed = current.activeSession.copy(endTimeMillis = now)
        val nextSession = Session(
            id = now + 1,
            tag = nextTag,
            startTimeMillis = now
        )

        _uiState.update {
            it.copy(activeSession = nextSession, selectedTag = nextTag)
        }

        persistCompleted(completed)
    }

    private fun persistCompleted(completed: Session) {
        viewModelScope.launch {
            runCatching {
                sessionRepository.upsert(completed)
            }.onFailure { error ->
                Log.e(TAG, "Failed to persist completed session", error)
            }
        }
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

