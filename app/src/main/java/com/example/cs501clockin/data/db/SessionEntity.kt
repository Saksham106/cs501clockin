package com.example.cs501clockin.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: Long,
    val tag: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long?,
    val notes: String?,
    val edited: Boolean
)

