package com.example.cs501clockin.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.cs501clockin.ClockInApp
import com.example.cs501clockin.MainActivity
import com.example.cs501clockin.R
import com.example.cs501clockin.data.repo.homeScreenTagChips
import com.example.cs501clockin.model.durationMillis
import com.example.cs501clockin.ui.util.formatDurationMillis
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Home screen widget: scrollable list of the same tags as Home (`homeScreenTagChips()`).
 * Tapping a tag calls [WidgetTagClickReceiver], which runs [ActiveSessionStore.switchTo].
 */
class TagSwitchWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        private const val OPEN_APP_REQUEST_CODE = 9100
        private const val LIST_CLICK_REQUEST_CODE = 4011

        fun requestUpdateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(
                ComponentName(context, TagSwitchWidgetProvider::class.java)
            )
            if (ids.isEmpty()) return
            val provider = TagSwitchWidgetProvider()
            provider.onUpdate(context, mgr, ids)
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val app = context.applicationContext as? ClockInApp ?: return

            val prefsData = runBlocking { app.userPreferencesRepository.data.first() }
            val tags = prefsData.homeScreenTagChips()
            TagWidgetTagCache.tags = tags
            TagWidgetTagCache.tagColorArgbByTag = prefsData.customTagColors

            val session = app.activeSessionStore.activeSession.value

            val views = RemoteViews(context.packageName, R.layout.widget_tag_switch)

            val elapsed = formatDurationMillis(session.durationMillis())
            views.setTextViewText(
                R.id.widget_session_line,
                context.getString(R.string.widget_session_line, session.tag, elapsed)
            )

            views.setRemoteAdapter(
                R.id.widget_tag_list,
                Intent(context, TagWidgetRemoteViewsService::class.java)
            )

            val mutableFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE
            } else {
                0
            }
            val listClickIntent = Intent(context, WidgetTagClickReceiver::class.java).apply {
                action = WidgetTagClickReceiver.ACTION_WIDGET_SWITCH_TAG
            }
            val listClickPending = PendingIntent.getBroadcast(
                context,
                LIST_CLICK_REQUEST_CODE,
                listClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or mutableFlag
            )
            views.setPendingIntentTemplate(R.id.widget_tag_list, listClickPending)

            views.setOnClickPendingIntent(R.id.widget_brand, openAppPendingIntent(context))
            views.setOnClickPendingIntent(R.id.widget_session_line, openAppPendingIntent(context))

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_tag_list)
        }

        private fun openAppPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            return PendingIntent.getActivity(context, OPEN_APP_REQUEST_CODE, intent, flags)
        }
    }
}
