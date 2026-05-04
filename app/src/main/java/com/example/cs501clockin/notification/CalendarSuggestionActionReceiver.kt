package com.example.cs501clockin.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.cs501clockin.ClockInApp

class CalendarSuggestionActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val app = context.applicationContext as ClockInApp
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = intent?.getIntExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID) ?: NOTIFICATION_ID

        when (intent?.action) {
            ACTION_ACCEPT -> {
                val tag = intent.getStringExtra(EXTRA_TAG)
                if (!tag.isNullOrBlank()) {
                    app.activeSessionStore.switchTo(tag)
                }
                nm.cancel(notificationId)
            }

            ACTION_DISMISS -> {
                nm.cancel(notificationId)
            }
        }
    }

    companion object {
        const val ACTION_ACCEPT = "com.example.cs501clockin.action.ACCEPT_CALENDAR_SUGGESTION"
        const val ACTION_DISMISS = "com.example.cs501clockin.action.DISMISS_CALENDAR_SUGGESTION"
        const val EXTRA_TAG = "extra_tag"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val NOTIFICATION_ID = 2400
    }
}
