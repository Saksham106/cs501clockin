package com.example.cs501clockin.calendar

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.cs501clockin.data.repo.CalendarTagRule
import com.example.cs501clockin.notification.CalendarSuggestionReceiver

class CalendarSuggestionScheduler(
    private val context: Context,
    private val repository: CalendarEventRepository
) {
    fun scheduleNext(rules: List<CalendarTagRule>) {
        val now = System.currentTimeMillis()
        val next = repository.findNextMatchingEvent(now, rules)
        if (next == null) {
            Log.d(TAG, "No matching event found; canceling alarm.")
            cancel()
            return
        }
        Log.d(
            TAG,
            "Scheduling alarm for ${next.startTimeMillis} (eventId=${next.eventId}, title=\"${next.title}\", tag=${next.matchedTag})"
        )
        scheduleAt(next.startTimeMillis)
    }

    fun cancel() {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(buildPendingIntent())
        Log.d(TAG, "Canceled calendar suggestion alarm.")
    }

    private fun scheduleAt(triggerAtMillis: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    Log.d(TAG, "Exact alarms not allowed; using inexact alarm.")
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm not permitted; falling back to inexact.", e)
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun buildPendingIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, CalendarSuggestionReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private companion object {
        const val REQUEST_CODE = 3100
        const val TAG = "CalSuggestScheduler"
    }
}
