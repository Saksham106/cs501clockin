package com.example.cs501clockin.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.example.cs501clockin.data.repo.CalendarTagRule

private const val LOOKAHEAD_DAYS = 7

data class CalendarEventSuggestion(
    val eventId: Long,
    val title: String,
    val startTimeMillis: Long,
    val matchedTag: String
)

class CalendarEventRepository(
    private val context: Context
) {
    fun hasReadCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun findNextMatchingEvent(startMillis: Long, rules: List<CalendarTagRule>): CalendarEventSuggestion? {
        if (!hasReadCalendarPermission()) return null
        if (rules.isEmpty()) return null

        val endMillis = startMillis + LOOKAHEAD_DAYS * 24L * 60L * 60L * 1000L
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().apply {
            ContentUris.appendId(this, startMillis)
            ContentUris.appendId(this, endMillis)
        }.build()

        val projection = arrayOf(
            CalendarContract.Instances._ID,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.ALL_DAY
        )

        val selection = "${CalendarContract.Instances.BEGIN} >= ?"
        val selectionArgs = arrayOf(startMillis.toString())
        val sortOrder = "${CalendarContract.Instances.BEGIN} ASC"

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances._ID)
            val beginCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val titleCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val allDayCol = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)

            while (cursor.moveToNext()) {
                val allDay = cursor.getInt(allDayCol) == 1
                if (allDay) continue
                val title = cursor.getString(titleCol) ?: ""
                if (title.isBlank()) continue

                val lowerTitle = title.lowercase()
                val matched = rules.firstOrNull { lowerTitle.contains(it.keyword) } ?: continue
                val eventId = cursor.getLong(idCol)
                val begin = cursor.getLong(beginCol)

                return CalendarEventSuggestion(
                    eventId = eventId,
                    title = title,
                    startTimeMillis = begin,
                    matchedTag = matched.tag
                )
            }
        }
        return null
    }
}
