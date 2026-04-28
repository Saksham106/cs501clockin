package com.example.cs501clockin.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cs501clockin.model.Session
import com.example.cs501clockin.model.SessionTags
import com.example.cs501clockin.model.durationMillis
import com.example.cs501clockin.ui.util.TagPalette
import com.example.cs501clockin.ui.util.formatClockTime
import com.example.cs501clockin.ui.util.formatDurationMillis
import com.example.cs501clockin.viewmodel.LocationSuggestion

@Composable
fun HomeScreen(
    tags: List<String>,
    selectedTag: String,
    activeSession: Session,
    onTagSelected: (String) -> Unit,
    onStart: () -> Unit,
    onEnd: () -> Unit,
    locationSuggestion: LocationSuggestion? = null,
    onAcceptLocationSuggestion: ((String) -> Unit)? = null,
    onDismissLocationSuggestion: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (locationSuggestion != null && onAcceptLocationSuggestion != null && onDismissLocationSuggestion != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Near ${locationSuggestion.label}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Start a ${locationSuggestion.suggestedTag} session?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onAcceptLocationSuggestion(locationSuggestion.suggestedTag) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Accept")
                        }
                        Button(
                            onClick = onDismissLocationSuggestion,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Switch Tag",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                tags.chunked(2).forEach { rowTags ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowTags.forEach { tag ->
                            val accent = TagPalette.colorFor(tag)
                            val selected = tag == selectedTag
                            Button(
                                onClick = { onTagSelected(tag) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) {
                                        accent.copy(alpha = 0.34f)
                                    } else {
                                        accent.copy(alpha = 0.16f)
                                    },
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(tag)
                            }
                        }
                        if (rowTags.size == 1) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Button(
            onClick = onStart,
            enabled = selectedTag != activeSession.tag,
            modifier = Modifier.fillMaxWidth()
        ) {
            val text = if (selectedTag == activeSession.tag) {
                "Currently on ${activeSession.tag}"
            } else {
                "Switch to $selectedTag"
            }
            Text(text)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Active: ${activeSession.tag}", style = MaterialTheme.typography.titleMedium)
                Text("Started: ${formatClockTime(activeSession.startTimeMillis)}")
                Text("Elapsed: ${formatDurationMillis(activeSession.durationMillis())}")
                HorizontalDivider()
                Button(
                    onClick = onEnd,
                    enabled = activeSession.tag != SessionTags.IDLE,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (activeSession.tag == SessionTags.IDLE) "Already Idle" else "Switch to Idle")
                }
            }
        }

    }
}

