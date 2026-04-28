package com.example.cs501clockin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cs501clockin.data.repo.UserPreferencesRepository
import com.example.cs501clockin.data.state.ActiveSessionStore
import com.example.cs501clockin.model.Session
import com.example.cs501clockin.model.SessionTags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    private val activeSessionStore: ActiveSessionStore,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedTag = MutableStateFlow(SessionTags.IDLE)

    init {
        viewModelScope.launch {
            activeSessionStore.activeSession.collect { active ->
                if (_selectedTag.value != active.tag) {
                    _selectedTag.value = active.tag
                }
            }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        activeSessionStore.activeSession,
        _selectedTag,
        userPreferencesRepository.data
    ) { active, selected, prefs ->
        val orderedVisible = prefs.allTags.filter { it in prefs.homeVisibleTags }
        val visible = if (orderedVisible.isEmpty()) SessionTags.defaults else orderedVisible
        val safeSelected = if (selected in visible) selected else (visible.firstOrNull() ?: SessionTags.IDLE)
        HomeUiState(
            tags = visible,
            selectedTag = safeSelected,
            activeSession = active
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun onTagSelected(tag: String) {
        _selectedTag.value = tag
    }

    fun startSession() {
        val tag = _selectedTag.value
        activeSessionStore.switchTo(tag)
        _selectedTag.update { tag }
    }

    fun endSession(): Session? {
        val completed = activeSessionStore.endActive()
        _selectedTag.value = SessionTags.IDLE
        return completed
    }
}

class HomeViewModelFactory(
    private val activeSessionStore: ActiveSessionStore,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(activeSessionStore, userPreferencesRepository) as T
    }
}
