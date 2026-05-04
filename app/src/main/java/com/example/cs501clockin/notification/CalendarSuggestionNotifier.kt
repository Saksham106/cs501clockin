package com.example.cs501clockin.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.cs501clockin.MainActivity
import com.example.cs501clockin.R

object CalendarSuggestionNotifier {
    private const val CHANNEL_ID = "clockin_calendar_suggestions"

    fun notify(context: Context, title: String, suggestedTag: String, notificationId: Int) {
        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val acceptIntent = PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, CalendarSuggestionActionReceiver::class.java).apply {
                action = CalendarSuggestionActionReceiver.ACTION_ACCEPT
                putExtra(CalendarSuggestionActionReceiver.EXTRA_TAG, suggestedTag)
                putExtra(CalendarSuggestionActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, CalendarSuggestionActionReceiver::class.java).apply {
                action = CalendarSuggestionActionReceiver.ACTION_DISMISS
                putExtra(CalendarSuggestionActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Calendar: $title")
            .setContentText("Switch to $suggestedTag?")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Calendar: $title\nSwitch to $suggestedTag?")
            )
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .addAction(0, "Switch to $suggestedTag", acceptIntent)
            .addAction(0, "Dismiss", dismissIntent)
            .build()

        nm.notify(notificationId, n)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = nm.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        val ch = NotificationChannel(
            CHANNEL_ID,
            "Calendar suggestions",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Suggest switching tags based on calendar events."
        }
        nm.createNotificationChannel(ch)
    }
}
