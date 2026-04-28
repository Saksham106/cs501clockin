package com.example.cs501clockin.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.cs501clockin.ClockInApp

class SuggestionActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val app = context.applicationContext as ClockInApp
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (intent?.action) {
            ACTION_ACCEPT -> {
                val tag = intent.getStringExtra(EXTRA_TAG)
                if (!tag.isNullOrBlank()) {
                    app.activeSessionStore.switchTo(tag)
                }
                nm.cancel(NOTIFICATION_ID)
            }

            ACTION_DISMISS -> {
                nm.cancel(NOTIFICATION_ID)
            }
        }
    }

    companion object {
        const val ACTION_ACCEPT = "com.example.cs501clockin.action.ACCEPT_LOCATION_SUGGESTION"
        const val ACTION_DISMISS = "com.example.cs501clockin.action.DISMISS_LOCATION_SUGGESTION"
        const val EXTRA_TAG = "extra_tag"
        const val NOTIFICATION_ID = 2002
    }
}

