package com.example.cs501clockin.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.example.cs501clockin.ClockInApp
import com.example.cs501clockin.data.repo.homeScreenTagChips
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Handles widget chip taps: validates tag against the same list as Home, then switches active session.
 */
class WidgetTagClickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_WIDGET_SWITCH_TAG) return
        val tag = intent.getStringExtra(EXTRA_TAG) ?: return
        val app = context.applicationContext as? ClockInApp ?: return

        val allowed = runBlocking {
            app.userPreferencesRepository.data.first().homeScreenTagChips()
        }
        if (tag !in allowed) return

        val pendingResult = goAsync()
        Handler(Looper.getMainLooper()).post {
            try {
                app.activeSessionStore.switchTo(tag)
                TagSwitchWidgetProvider.requestUpdateAll(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_SWITCH_TAG = "com.example.cs501clockin.action.WIDGET_SWITCH_TAG"
        const val EXTRA_TAG = "widget_extra_tag"
    }
}
