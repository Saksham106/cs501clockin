package com.example.cs501clockin.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.cs501clockin.ClockInApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

private const val NOTIFY_WINDOW_MILLIS = 5 * 60 * 1000L
private const val LOOKBACK_WINDOW_MILLIS = 2 * 60 * 1000L

class CalendarSuggestionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        val app = context.applicationContext as ClockInApp
        Log.d(TAG, "Alarm received for calendar suggestion.")
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val prefs = app.userPreferencesRepository.data.first()
                if (!prefs.calendarSuggestionsEnabled || prefs.calendarTagRules.isEmpty()) {
                    Log.d(TAG, "Calendar suggestions disabled or no rules; canceling.")
                    app.calendarSuggestionScheduler.cancel()
                    return@launch
                }

                val now = System.currentTimeMillis()
                val candidate = app.calendarEventRepository.findNextMatchingEvent(
                    now - LOOKBACK_WINDOW_MILLIS,
                    prefs.calendarTagRules
                )

                if (candidate != null) {
                    val inWindow = now >= candidate.startTimeMillis &&
                        now <= candidate.startTimeMillis + NOTIFY_WINDOW_MILLIS
                    val activeTag = app.activeSessionStore.activeSession.value.tag
                    Log.d(
                        TAG,
                        "Candidate eventId=${candidate.eventId}, title=\"${candidate.title}\", tag=${candidate.matchedTag}, inWindow=$inWindow, activeTag=$activeTag"
                    )
                    if (inWindow && activeTag != candidate.matchedTag) {
                        CalendarSuggestionNotifier.notify(
                            context = context,
                            title = candidate.title,
                            suggestedTag = candidate.matchedTag,
                            notificationId = CalendarSuggestionActionReceiver.NOTIFICATION_ID
                        )
                        Log.d(TAG, "Notification posted for tag ${candidate.matchedTag}.")
                    } else {
                        Log.d(TAG, "Notification skipped (inWindow=$inWindow, activeTag=$activeTag).")
                    }
                } else {
                    Log.d(TAG, "No matching calendar event found.")
                }

                app.calendarSuggestionScheduler.scheduleNext(prefs.calendarTagRules)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "CalSuggestReceiver"
    }
}
