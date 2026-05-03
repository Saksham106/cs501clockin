package com.example.cs501clockin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.cs501clockin.model.Session
import com.example.cs501clockin.model.durationMillis
import com.example.cs501clockin.ui.util.TagPalette
import com.example.cs501clockin.ui.util.formatClockTime
import com.example.cs501clockin.ui.util.formatDurationMillis
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    sessions: List<Session>,
    tagColorArgbByTag: Map<String, Int> = emptyMap(),
    onSessionClick: (Session) -> Unit,
    modifier: Modifier = Modifier
) {
    val startOfToday = rememberStartOfTodayMillis()
    var dayOffset by rememberSaveable { mutableIntStateOf(0) }

    val selectedDayStart = startOfToday + (dayOffset * DAY_MILLIS)
    val selectedDayEnd = selectedDayStart + DAY_MILLIS

    val daySessions = remember(sessions, selectedDayStart, selectedDayEnd) {
        sessions
            .filter { it.startTimeMillis in selectedDayStart until selectedDayEnd }
            .sortedByDescending { it.startTimeMillis }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { dayOffset -= 1 }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous day")
                }
                Surface(
                    tonalElevation = 2.dp,
                    shadowElevation = 0.dp,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = if (dayOffset == 0) "Today" else formatHistoryDay(selectedDayStart),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                IconButton(onClick = { if (dayOffset < 0) dayOffset += 1 }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next day")
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${daySessions.size} sessions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (daySessions.isEmpty()) {
            item {
                Text(
                    text = if (dayOffset == 0) {
                        "No sessions today yet. Start one from Home."
                    } else {
                        "No sessions for ${formatHistoryDay(selectedDayStart)}."
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(daySessions, key = { it.id }) { session ->
                val accent = TagPalette.colorFor(session.tag, tagColorArgbByTag)
                // Match the Home screen's pastel tag styling (tinted, not saturated).
                val container = accent.copy(alpha = 0.16f)
                val shape = RoundedCornerShape(16.dp)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSessionClick(session) },
                    colors = CardDefaults.cardColors(
                        containerColor = container,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = shape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(session.tag, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${formatClockTime(session.startTimeMillis)} - ${
                                session.endTimeMillis?.let(::formatClockTime) ?: "Now"
                            }"
                        )
                        Text("Duration: ${formatDurationMillis(session.durationMillis())}")
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberStartOfTodayMillis(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun formatHistoryDay(epochMillis: Long): String {
    val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
    return formatter.format(Date(epochMillis))
}

private const val DAY_MILLIS = 24L * 60L * 60L * 1000L

