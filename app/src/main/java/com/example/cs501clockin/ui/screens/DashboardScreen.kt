package com.example.cs501clockin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cs501clockin.model.Session
import com.example.cs501clockin.model.durationMillis
import com.example.cs501clockin.ui.util.formatDurationMillis
import java.util.Calendar

@Composable
fun DashboardScreen(
    sessions: List<Session>,
    modifier: Modifier = Modifier
) {
    val startOfToday = rememberStartOfTodayMillis()
    val todays = sessions.filter { it.startTimeMillis >= startOfToday }

    val totals = todays
        .filter { it.tag != "Idle" }
        .groupBy { it.tag }
        .mapValues { (_, ss) -> ss.sumOf { it.durationMillis() } }
        .toList()
        .sortedByDescending { it.second }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Dashboard", style = MaterialTheme.typography.titleLarge)
        Text("Today totals by tag", style = MaterialTheme.typography.titleMedium)

        if (totals.isEmpty()) {
            Text("No sessions yet. Start tracking to see totals.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(totals, key = { it.first }) { (tag, duration) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(tag, style = MaterialTheme.typography.titleMedium)
                            Text(formatDurationMillis(duration))
                        }
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

