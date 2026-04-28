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

object LocationSuggestionNotifier {
    private const val CHANNEL_ID = "clockin_location_suggestions"

    fun notify(context: Context, label: String, suggestedTag: String) {
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
            Intent(context, SuggestionActionReceiver::class.java).apply {
                action = SuggestionActionReceiver.ACTION_ACCEPT
                putExtra(SuggestionActionReceiver.EXTRA_TAG, suggestedTag)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, SuggestionActionReceiver::class.java).apply {
                action = SuggestionActionReceiver.ACTION_DISMISS
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Near $label")
            .setContentText("Start a $suggestedTag session?")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Near $label\nStart a $suggestedTag session?")
            )
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .addAction(0, "Start $suggestedTag", acceptIntent)
            .addAction(0, "Dismiss", dismissIntent)
            .build()

        nm.notify(SuggestionActionReceiver.NOTIFICATION_ID, n)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = nm.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        val ch = NotificationChannel(
            CHANNEL_ID,
            "Location suggestions",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Suggest starting a session when near a saved location."
        }
        nm.createNotificationChannel(ch)
    }
}

