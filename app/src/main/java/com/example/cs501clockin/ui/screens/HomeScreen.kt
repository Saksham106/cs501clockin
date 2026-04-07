package com.example.cs501clockin.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cs501clockin.location.LocationResult
import com.example.cs501clockin.model.Session
import com.example.cs501clockin.model.SessionTags
import com.example.cs501clockin.ui.util.formatClockTime

@Composable
fun HomeScreen(
    tags: List<String>,
    selectedTag: String,
    activeSession: Session?,
    onTagSelected: (String) -> Unit,
    onStart: () -> Unit,
    onEnd: () -> Unit,
    locationState: com.example.cs501clockin.viewmodel.LocationUiState? = null,
    onRequestLocationPermission: (() -> Unit)? = null,
    onRefreshLocation: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Quick Start", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Tap a tag and start tracking what you are actually doing.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.take(3).forEach { tag ->
                FilterChip(
                    selected = tag == selectedTag,
                    onClick = { onTagSelected(tag) },
                    label = { Text(tag) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.drop(3).forEach { tag ->
                FilterChip(
                    selected = tag == selectedTag,
                    onClick = { onTagSelected(tag) },
                    label = { Text(tag) }
                )
            }
        }

        Button(
            onClick = onStart,
            enabled = selectedTag != SessionTags.IDLE && activeSession == null,
            modifier = Modifier.fillMaxWidth()
        ) {
            val text = when {
                activeSession != null -> "Session Running"
                selectedTag == SessionTags.IDLE -> "Select a task tag"
                else -> "Start $selectedTag Session"
            }
            Text(text)
        }

        if (activeSession != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Active: ${activeSession.tag}")
                    Text("Started: ${formatClockTime(activeSession.startTimeMillis)}")
                    Button(onClick = onEnd, modifier = Modifier.fillMaxWidth()) {
                        Text("End Session")
                    }
                }
            }
        }

        if (activeSession == null && selectedTag == SessionTags.IDLE) {
            Text(
                text = "Status: Idle",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        if (locationState != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Location", style = MaterialTheme.typography.titleMedium)
                    when (val result = locationState.result) {
                        null -> {
                            Text("Not requested yet.")
                            Button(
                                onClick = { onRequestLocationPermission?.invoke() },
                                enabled = onRequestLocationPermission != null,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Enable location") }
                        }

                        is LocationResult.PermissionDenied -> {
                            Text("Permission denied. Enable it to use location features.")
                            Button(
                                onClick = { onRequestLocationPermission?.invoke() },
                                enabled = onRequestLocationPermission != null,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Grant permission") }
                        }

                        is LocationResult.Error -> {
                            Text("Error: ${result.message}")
                            Button(
                                onClick = { onRefreshLocation?.invoke() },
                                enabled = onRefreshLocation != null,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Retry") }
                        }

                        is LocationResult.Success -> {
                            Text("Lat: ${"%.5f".format(result.latLng.latitude)}")
                            Text("Lon: ${"%.5f".format(result.latLng.longitude)}")
                            Button(
                                onClick = { onRefreshLocation?.invoke() },
                                enabled = onRefreshLocation != null && !locationState.isLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(if (locationState.isLoading) "Refreshing..." else "Refresh location") }
                        }
                    }
                }
            }
        }

    }
}

