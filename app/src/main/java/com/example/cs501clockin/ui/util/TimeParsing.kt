package com.example.cs501clockin.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Parses an HH:mm time string (e.g. 09:30) and returns epoch millis on the same local date
 * as [baseEpochMillis]. Returns null if parsing fails.
 */
fun parseSameDayTimeToEpochMillis(
    baseEpochMillis: Long,
    hhmm: String
): Long? {
    val time = try {
        LocalTime.parse(hhmm.trim())
    } catch (_: Throwable) {
        return null
    }
    val zone = ZoneId.systemDefault()
    val baseDate: LocalDate = Instant.ofEpochMilli(baseEpochMillis).atZone(zone).toLocalDate()
    return baseDate.atTime(time).atZone(zone).toInstant().toEpochMilli()
}

