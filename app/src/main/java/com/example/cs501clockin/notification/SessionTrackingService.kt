package com.example.cs501clockin.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.cs501clockin.ClockInApp
import com.example.cs501clockin.MainActivity
import com.example.cs501clockin.R
import com.example.cs501clockin.model.Session
import com.example.cs501clockin.model.SessionTags
import com.example.cs501clockin.model.durationMillis
import com.example.cs501clockin.ui.util.formatClockTime
import com.example.cs501clockin.ui.util.formatDurationMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SessionTrackingService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Main.immediate)

    @Volatile
    private var latestQuickTags: Set<String> = SessionTags.defaults.toSet()
    @Volatile
    private var latestAllTags: List<String> = SessionTags.defaults

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        val app = application as ClockInApp
        val store = app.activeSessionStore

        serviceScope.launch {
            combine(
                store.activeSession,
                app.userPreferencesRepository.data
            ) { session, prefs ->
                Triple(session, prefs.notificationQuickTags, prefs.allTags)
            }.collectLatest { (session, quickTags, allTags) ->
                latestQuickTags = quickTags
                latestAllTags = allTags
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(session, quickTags, allTags)
                )
            }
        }

        serviceScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(60_000L)
                val clockInApp = application as ClockInApp
                val s = clockInApp.activeSessionStore.activeSession.value
                val tags = latestQuickTags
                val allTags = latestAllTags
                withContext(Dispatchers.Main) {
                    val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    nm.notify(NOTIFICATION_ID, buildNotification(s, tags, allTags))
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as ClockInApp
        when (intent?.action) {
            ACTION_SWITCH_TAG -> {
                val tag = intent.getStringExtra(EXTRA_TAG)
                if (!tag.isNullOrBlank() && tag in latestAllTags) {
                    app.activeSessionStore.switchTo(tag)
                }
            }
        }

        val session = app.activeSessionStore.activeSession.value
        startForeground(
            NOTIFICATION_ID,
            buildNotification(session, latestQuickTags, latestAllTags)
        )
        return START_STICKY
    }

    override fun onDestroy() {
        runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
        serviceJob.cancel()
        super.onDestroy()
    }

    private fun buildNotification(session: Session, quickTags: Set<String>, allTags: List<String>): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val elapsed = formatDurationMillis(session.durationMillis())
        val started = formatClockTime(session.startTimeMillis)

        // quickTags is capped to 3 in preferences; Android still only highlights a few actions in the collapsed shade.
        val actionTags = quickTags
            .asSequence()
            .filter { it in allTags && it != session.tag }
            .sorted()
            .toList()

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Tracking: ${session.tag}")
            .setContentText("Started $started · $elapsed")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        buildString {
                            append("Started $started · $elapsed")
                            if (actionTags.isEmpty()) {
                                append("\nAdd tag buttons in Settings → Notifications.")
                            } else {
                                append("\nUse the actions below to switch tag.")
                            }
                        }
                    )
            )
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(contentIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }

        actionTags.forEach { tag ->
            val switchIntent = Intent(this, SessionTrackingService::class.java).apply {
                action = ACTION_SWITCH_TAG
                putExtra(EXTRA_TAG, tag)
            }
            val idx = allTags.indexOf(tag).coerceAtLeast(0)
            val requestCode = REQUEST_CODE_SWITCH_BASE + idx
            val switchPendingIntent = PendingIntent.getService(
                this,
                requestCode,
                switchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_menu_rotate,
                tag,
                switchPendingIntent
            )
        }

        return builder.build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Session tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ongoing session tracking; cannot be dismissed while a session is active."
                setLockscreenVisibility(Notification.VISIBILITY_PUBLIC)
            }
            nm.createNotificationChannel(ch)
        }
    }

    companion object {
        private const val CHANNEL_ID = "clockin_session_v2"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_SWITCH_TAG = "com.example.cs501clockin.action.SWITCH_SESSION_TAG"
        private const val EXTRA_TAG = "extra_tag"
        private const val REQUEST_CODE_SWITCH_BASE = 200

        fun start(context: Context) {
            val i = Intent(context, SessionTrackingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i)
            } else {
                context.startService(i)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, SessionTrackingService::class.java))
        }
    }
}
