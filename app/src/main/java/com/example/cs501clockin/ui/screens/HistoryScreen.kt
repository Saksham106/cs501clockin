package com.example.cs501clockin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import com.example.cs501clockin.ui.util.formatClockTime
import com.example.cs501clockin.ui.util.formatDurationMillis

@Composable
fun HistoryScreen(
    sessions: List<Session>,
    onSessionClick: (Session) -> Unit,
    modifier: Modifier = Modifier
) {
    if (sessions.isEmpty()) {
        Text(
            text = "No sessions yet. Start one from Home.",
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.secondary
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(sessions, key = { it.id }) { session ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSessionClick(session) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
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

