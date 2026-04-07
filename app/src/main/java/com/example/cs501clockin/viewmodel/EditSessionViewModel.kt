package com.example.cs501clockin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cs501clockin.data.repo.SessionRepository
import com.example.cs501clockin.model.Session
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditSessionViewModel(
    private val sessionId: Long,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private companion object {
        const val TAG = "EditSessionViewModel"
    }

    val session: StateFlow<Session?> =
        sessionRepository.observeSession(sessionId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(updated: Session) {
        viewModelScope.launch {
            runCatching { sessionRepository.upsert(updated) }
                .onFailure { error -> Log.e(TAG, "Failed to save session", error) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            runCatching { sessionRepository.deleteById(sessionId) }
                .onFailure { error -> Log.e(TAG, "Failed to delete session", error) }
        }
    }
}

class EditSessionViewModelFactory(
    private val sessionId: Long,
    private val sessionRepository: SessionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditSessionViewModel(sessionId, sessionRepository) as T
    }
}

