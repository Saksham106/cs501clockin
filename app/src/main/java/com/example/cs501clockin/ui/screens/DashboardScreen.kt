package com.example.cs501clockin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import com.example.cs501clockin.model.Session
import com.example.cs501clockin.model.SessionTags
import com.example.cs501clockin.model.durationMillis
import com.example.cs501clockin.ui.util.TagPalette
import com.example.cs501clockin.ui.util.formatClockTime
import com.example.cs501clockin.ui.util.formatDurationMillis
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    sessions: List<Session>,
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

    val totalsByTag = remember(daySessions) {
        daySessions
            .groupBy { it.tag }
            .mapValues { (_, ss) -> ss.sumOf { it.durationMillis() } }
    }

    val orderedTags = remember(totalsByTag) {
        TagPalette.orderedWithFallback(totalsByTag.keys)
    }

    val orderedTotals = remember(orderedTags, totalsByTag) {
        orderedTags.mapNotNull { tag ->
            totalsByTag[tag]?.let { duration -> tag to duration }
        }
    }

    val totalTracked = remember(daySessions) { daySessions.sumOf { it.durationMillis() } }

    val timelineItems = remember(daySessions) { daySessions }

    val longestNonIdle = remember(daySessions) {
        daySessions
            .filter { it.tag != SessionTags.IDLE }
            .maxOfOrNull { it.durationMillis() }
            ?: 0L
    }

    val sevenDayStats = remember(sessions, startOfToday) {
        computeSevenDayStats(sessions, startOfToday)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { dayOffset -= 1 }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous day")
                }
                Surface(
                    tonalElevation = 2.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (dayOffset == 0) "Today" else formatDashboardDay(selectedDayStart),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                IconButton(onClick = { if (dayOffset < 0) dayOffset += 1 }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next day")
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Day Overview",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "  •  ${formatDayAndWeekday(selectedDayStart)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Tracked", style = MaterialTheme.typography.titleLarge)
                        Text(
                            formatDurationMillis(totalTracked),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    HorizontalDivider()

                    if (orderedTotals.isEmpty()) {
                        Text("No sessions tracked for this day.")
                    } else {
                        orderedTotals.forEach { (tag, duration) ->
                            DashboardTagRow(tag = tag, duration = duration)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Timeline", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Longest segment (non-idle): ${formatDurationMillis(longestNonIdle)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (timelineItems.isEmpty()) {
                        Text("No timeline entries for this day.")
                    }
                }
            }
        }

        items(timelineItems, key = { it.id }) { session ->
            TimelineItem(session = session)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Last 7 Days - Active Overview", style = MaterialTheme.typography.headlineSmall)
                    Text("Excludes today", color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Avg Active (tracked days - ${sevenDayStats.trackedDays}/7)",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            formatDurationMillis(sevenDayStats.averageActivePerTrackedDay),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    SegmentedActiveBar(
                        totals = sevenDayStats.nonIdleTotals,
                        totalNonIdle = sevenDayStats.totalNonIdle
                    )

                    Text(
                        "Total - ${formatDurationMillis(sevenDayStats.totalAllTags)}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text("Avg per tag (tracked days)", style = MaterialTheme.typography.titleLarge)
                    sevenDayStats.averageByTag.forEach { (tag, avgDuration) ->
                        DashboardTagRow(tag = tag, duration = avgDuration)
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

@Composable
private fun DashboardTagRow(tag: String, duration: Long) {
    val dotColor = TagPalette.colorFor(tag)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(tag, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
        Text(
            formatDurationMillis(duration),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TimelineItem(session: Session) {
    val tagColor = TagPalette.colorFor(session.tag)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatClockTime(session.startTimeMillis),
            modifier = Modifier.width(74.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tagColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = tagColor.copy(alpha = 0.22f)
        ) {
            Text(
                text = session.tag,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = formatDurationMillis(session.durationMillis()),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SegmentedActiveBar(
    totals: Map<String, Long>,
    totalNonIdle: Long
) {
    if (totalNonIdle <= 0L || totals.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
        )
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp)
            .clip(RoundedCornerShape(14.dp))
    ) {
        totals
            .toList()
            .sortedWith(compareBy({ TagPalette.sortIndex(it.first) }, { it.first }))
            .forEach { (tag, duration) ->
                Box(
                    modifier = Modifier
                        .weight(duration.toFloat())
                        .fillMaxSize()
                        .background(TagPalette.colorFor(tag))
                )
            }
    }
}

private data class SevenDayStats(
    val trackedDays: Int,
    val totalNonIdle: Long,
    val totalAllTags: Long,
    val nonIdleTotals: Map<String, Long>,
    val averageByTag: List<Pair<String, Long>>,
    val averageActivePerTrackedDay: Long
)

private fun computeSevenDayStats(sessions: List<Session>, startOfToday: Long): SevenDayStats {
    val windowStart = startOfToday - (7L * DAY_MILLIS)
    val inWindow = sessions.filter { it.startTimeMillis in windowStart until startOfToday }

    val nonIdleByDay = inWindow
        .filter { it.tag != SessionTags.IDLE }
        .groupBy { startOfDayMillis(it.startTimeMillis) }
        .mapValues { (_, ss) -> ss.sumOf { it.durationMillis() } }

    val trackedDays = nonIdleByDay.values.count { it > 0L }
    val totalNonIdle = nonIdleByDay.values.sum()

    val totalsByTagAll = inWindow
        .groupBy { it.tag }
        .mapValues { (_, ss) -> ss.sumOf { it.durationMillis() } }

    val orderedTags = TagPalette.orderedWithFallback(totalsByTagAll.keys)
    val averageByTag = orderedTags.mapNotNull { tag ->
        totalsByTagAll[tag]?.let { total ->
            tag to if (trackedDays > 0) total / trackedDays else 0L
        }
    }

    val nonIdleTotals = totalsByTagAll
        .filterKeys { it != SessionTags.IDLE }

    val totalAllTags = totalsByTagAll.values.sum()

    return SevenDayStats(
        trackedDays = trackedDays,
        totalNonIdle = totalNonIdle,
        totalAllTags = totalAllTags,
        nonIdleTotals = nonIdleTotals,
        averageByTag = averageByTag,
        averageActivePerTrackedDay = if (trackedDays > 0) totalNonIdle / trackedDays else 0L
    )
}

private fun formatDayAndWeekday(epochMillis: Long): String {
    val formatter = SimpleDateFormat("MMM d - EEEE", Locale.getDefault())
    return formatter.format(Date(epochMillis))
}

private fun formatDashboardDay(epochMillis: Long): String {
    val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
    return formatter.format(Date(epochMillis))
}

private fun startOfDayMillis(epochMillis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = epochMillis
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private const val DAY_MILLIS = 24L * 60L * 60L * 1000L

