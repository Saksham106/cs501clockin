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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
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
    tagColorArgbByTag: Map<String, Int> = emptyMap(),
    onTagSelected: (String) -> Unit,
    onStart: () -> Unit,
    onEnd: () -> Unit,
    locationSuggestion: LocationSuggestion? = null,
    onAcceptLocationSuggestion: ((String) -> Unit)? = null,
    onDismissLocationSuggestion: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val darkTheme = isSystemInDarkTheme()
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
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        OutlinedButton(
                            onClick = onDismissLocationSuggestion,
                            modifier = Modifier.weight(1f)
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
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            val accent = TagPalette.colorFor(tag, tagColorArgbByTag)
                            val selected = tag == selectedTag
                            Button(
                                onClick = { onTagSelected(tag) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) {
                                        accent.copy(alpha = if (darkTheme) 0.58f else 0.34f)
                                    } else {
                                        accent.copy(alpha = if (darkTheme) 0.32f else 0.16f)
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
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurface
            )
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
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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

