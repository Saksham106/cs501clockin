package com.example.cs501clockin.viewmodel

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
    val session: StateFlow<Session?> =
        sessionRepository.observeSession(sessionId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(updated: Session) {
        viewModelScope.launch { sessionRepository.upsert(updated) }
    }

    fun delete() {
        viewModelScope.launch { sessionRepository.deleteById(sessionId) }
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

