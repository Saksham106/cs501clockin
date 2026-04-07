package com.example.cs501clockin.data.repo

import com.example.cs501clockin.data.db.SessionDao
import com.example.cs501clockin.data.db.SessionEntity
import com.example.cs501clockin.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionRepository(
    private val dao: SessionDao
) {
    fun observeSessions(): Flow<List<Session>> =
        dao.observeSessions().map { list -> list.map { it.toDomain() } }

    fun observeSession(id: Long): Flow<Session?> =
        dao.observeSession(id).map { it?.toDomain() }

    suspend fun upsert(session: Session) {
        dao.upsert(session.toEntity())
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }
}

private fun SessionEntity.toDomain(): Session =
    Session(
        id = id,
        tag = tag,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        notes = notes,
        edited = edited
    )

private fun Session.toEntity(): SessionEntity =
    SessionEntity(
        id = id,
        tag = tag,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        notes = notes,
        edited = edited
    )

