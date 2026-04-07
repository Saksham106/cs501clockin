package com.example.cs501clockin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cs501clockin.model.Session
import com.example.cs501clockin.ui.util.formatClockTime
import com.example.cs501clockin.ui.util.parseSameDayTimeToEpochMillis

@Composable
fun EditSessionScreen(
    session: Session?,
    onSave: (Session) -> Unit,
    onDelete: (Session) -> Unit,
    modifier: Modifier = Modifier
) {
    if (session == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Session not found.", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    var tag by remember(session.id) { mutableStateOf(session.tag) }
    var notes by remember(session.id) { mutableStateOf(session.notes.orEmpty()) }
    var startHhmm by remember(session.id) { mutableStateOf("") }
    var endHhmm by remember(session.id) { mutableStateOf("") }
    var timeError by remember(session.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(session.id) {
        tag = session.tag
        notes = session.notes.orEmpty()
        startHhmm = ""
        endHhmm = ""
        timeError = null
    }

    var showDeleteConfirm by remember(session.id) { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Edit Session", style = MaterialTheme.typography.titleLarge)
        Text(
            "Current: ${formatClockTime(session.startTimeMillis)} - ${
                session.endTimeMillis?.let(::formatClockTime) ?: "Now"
            }",
            color = MaterialTheme.colorScheme.secondary
        )

        OutlinedTextField(
            value = tag,
            onValueChange = { tag = it },
            label = { Text("Tag") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = startHhmm,
            onValueChange = {
                startHhmm = it
                timeError = null
            },
            label = { Text("Start time (HH:mm, optional)") },
            placeholder = { Text("e.g. 09:30") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = endHhmm,
            onValueChange = {
                endHhmm = it
                timeError = null
            },
            label = { Text("End time (HH:mm, optional)") },
            placeholder = { Text("e.g. 13:05") },
            modifier = Modifier.fillMaxWidth()
        )

        timeError?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val newStartMillis = if (startHhmm.isBlank()) {
                        session.startTimeMillis
                    } else {
                        parseSameDayTimeToEpochMillis(session.startTimeMillis, startHhmm)
                            ?: run {
                                timeError = "Invalid start time. Use HH:mm (example: 09:30)."
                                return@Button
                            }
                    }

                    val newEndMillis = if (endHhmm.isBlank()) {
                        session.endTimeMillis
                    } else {
                        parseSameDayTimeToEpochMillis(session.startTimeMillis, endHhmm)
                            ?: run {
                                timeError = "Invalid end time. Use HH:mm (example: 13:05)."
                                return@Button
                            }
                    }

                    if (newEndMillis != null && newEndMillis < newStartMillis) {
                        timeError = "End time must be after start time."
                        return@Button
                    }

                    val updated = session.copy(
                        tag = tag.ifBlank { session.tag },
                        startTimeMillis = newStartMillis,
                        endTimeMillis = newEndMillis,
                        notes = notes.ifBlank { null },
                        edited = true
                    )
                    onSave(updated)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Delete")
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete session?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(session)
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

