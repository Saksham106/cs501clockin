package com.example.cs501clockin.model

data class Session(
    val id: Long,
    val tag: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long? = null,
    val notes: String? = null,
    val edited: Boolean = false
)

object SessionTags {
    const val IDLE = "Idle"
    val defaults: List<String> = listOf(IDLE, "Study", "Class", "Gym", "Work", "Errands")
}

val Session.isActive: Boolean
    get() = endTimeMillis == null

fun Session.durationMillis(nowMillis: Long = System.currentTimeMillis()): Long {
    val end = endTimeMillis ?: nowMillis
    return (end - startTimeMillis).coerceAtLeast(0L)
}

