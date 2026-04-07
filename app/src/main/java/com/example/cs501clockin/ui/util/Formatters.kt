package com.example.cs501clockin.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatClockTime(epochMillis: Long): String {
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(Date(epochMillis))
}

fun formatDurationMillis(durationMillis: Long): String {
    val minutes = (durationMillis / 60_000L).coerceAtLeast(0L)
    val hours = minutes / 60L
    val remain = minutes % 60L
    return if (hours > 0) "${hours}h ${remain}m" else "${minutes}m"
}

