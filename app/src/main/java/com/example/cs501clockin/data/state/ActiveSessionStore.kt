package com.example.cs501clockin.data.state

import android.util.Log
import com.example.cs501clockin.data.repo.SessionRepository
import com.example.cs501clockin.model.Session
import com.example.cs501clockin.model.SessionTags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Single source of truth for the in-memory active session (not persisted until completed).
 */
class ActiveSessionStore(
    private val sessionRepository: SessionRepository
) {
    private companion object {
        const val TAG = "ActiveSessionStore"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _activeSession = MutableStateFlow(
        Session(
            id = System.currentTimeMillis(),
            tag = SessionTags.IDLE,
            startTimeMillis = System.currentTimeMillis()
        )
    )
    val activeSession: StateFlow<Session> = _activeSession.asStateFlow()

    fun switchTo(nextTag: String) {
        val current = _activeSession.value
        if (nextTag == current.tag) return

        val now = System.currentTimeMillis()
        val completed = current.copy(endTimeMillis = now)
        val nextSession = Session(
            id = now + 1,
            tag = nextTag,
            startTimeMillis = now
        )

        _activeSession.value = nextSession
        persistCompleted(completed)
    }

    /**
     * Ends the current non-idle session and switches to Idle. Returns the completed session or null.
     */
    fun endActive(): Session? {
        val current = _activeSession.value
        if (current.tag == SessionTags.IDLE) return null

        val now = System.currentTimeMillis()
        val completed = current.copy(endTimeMillis = now)
        val idleSession = Session(
            id = now + 1,
            tag = SessionTags.IDLE,
            startTimeMillis = now
        )
        _activeSession.value = idleSession
        persistCompleted(completed)
        return completed
    }

    private fun persistCompleted(completed: Session) {
        scope.launch {
            runCatching {
                sessionRepository.upsert(completed)
            }.onFailure { error ->
                Log.e(TAG, "Failed to persist completed session", error)
            }
        }
    }
}
