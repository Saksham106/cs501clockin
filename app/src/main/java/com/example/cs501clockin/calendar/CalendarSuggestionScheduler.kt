package com.example.cs501clockin.calendar

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
            cancel()
            return
        }
        scheduleAt(next.startTimeMillis)
    }

    fun cancel() {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(buildPendingIntent())
    }

    private fun scheduleAt(triggerAtMillis: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
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
    }
}
