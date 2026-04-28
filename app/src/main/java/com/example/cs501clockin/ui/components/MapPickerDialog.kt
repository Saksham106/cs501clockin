package com.example.cs501clockin.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapPickerDialog(
    title: String,
    initialLatLng: LatLng = LatLng(42.3601, -71.0589), // Boston default
    onDismiss: () -> Unit,
    onConfirm: (LatLng) -> Unit
) {
    var selected by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 14f)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Tap the map to drop a pin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng -> selected = latLng }
                ) {
                    val pin = selected
                    if (pin != null) {
                        Marker(
                            state = MarkerState(position = pin),
                            title = "Selected location"
                        )
                    }
                }
                if (selected != null) {
                    Text(
                        "Selected: ${"%.5f".format(selected!!.latitude)}, ${"%.5f".format(selected!!.longitude)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        "No location selected yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selected?.let(onConfirm) },
                enabled = selected != null
            ) { Text("Use this location") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

