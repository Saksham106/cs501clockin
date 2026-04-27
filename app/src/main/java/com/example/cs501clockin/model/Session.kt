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
    const val SCHOOL = "School"
    const val WORK = "Work"
    const val TRAINING = "Training"
    const val FOOD = "Food"
    const val PERSONAL_CARE = "Personal Care"
    const val RECOVERY_MIND = "Recovery / Mind"
    const val SOCIAL_ADMIN = "Social / Admin"
    const val IDLE = "Idle / Off"

    val defaults: List<String> = listOf(
        SCHOOL,
        WORK,
        TRAINING,
        FOOD,
        PERSONAL_CARE,
        RECOVERY_MIND,
        SOCIAL_ADMIN,
        IDLE
    )
}

val Session.isActive: Boolean
    get() = endTimeMillis == null

fun Session.durationMillis(nowMillis: Long = System.currentTimeMillis()): Long {
    val end = endTimeMillis ?: nowMillis
    return (end - startTimeMillis).coerceAtLeast(0L)
}

