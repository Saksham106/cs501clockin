package com.example.cs501clockin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cs501clockin.model.SessionTags
import com.example.cs501clockin.ui.components.MapPickerDialog
import com.example.cs501clockin.ui.util.PastelTagColors
import com.example.cs501clockin.ui.util.TagPalette
import com.example.cs501clockin.ui.util.argbToColor
import com.example.cs501clockin.viewmodel.SettingsUiState
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onNotificationsChanged: (Boolean) -> Unit,
    onLocationSuggestionsChanged: (Boolean) -> Unit,
    onNotificationQuickTagToggle: (tag: String, selected: Boolean) -> Unit,
    onHomeVisibleTagToggle: (tag: String, visible: Boolean) -> Unit,
    onAddCustomTag: (String, Int) -> Unit,
    onDeleteCustomTag: (String) -> Unit,
    onAddSavedLocation: (label: String, suggestedTag: String, radiusMeters: Int) -> Unit,
    onAddSavedLocationManual: (label: String, suggestedTag: String, radiusMeters: Int, latitude: Double, longitude: Double) -> Unit,
    onDeleteSavedLocation: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showMapDialog by remember { mutableStateOf(false) }
    var labelInput by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf(state.allTags.firstOrNull() ?: "") }
    var radiusInput by remember { mutableStateOf("150") }
    var tagMenuExpanded by remember { mutableStateOf(false) }
    var newTagInput by remember { mutableStateOf("") }
    var selectedPastelArgb by remember { mutableIntStateOf(PastelTagColors.CHOICES_ARGB.first()) }
    var pickedLatLng by remember { mutableStateOf<LatLng?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Notifications", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Shows an ongoing notification while a session is active (not swipe-dismissable). On Android 13+ you may need to allow notifications.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable session notification")
                        Switch(
                            checked = state.notificationsEnabled,
                            onCheckedChange = onNotificationsChanged
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        "Tags on notification",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "Pick 1–3 tags for notification quick actions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val atMaxQuickTags = state.notificationQuickTags.size >= 3
                    state.allTags.chunked(2).forEach { rowTags ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowTags.forEach { tag ->
                                val accent = TagPalette.colorFor(tag, state.customTagColors)
                                val selected = state.notificationQuickTags.contains(tag)
                                FilterChip(
                                    selected = selected,
                                    onClick = { onNotificationQuickTagToggle(tag, !selected) },
                                    enabled = selected || !atMaxQuickTags,
                                    label = { Text(tag, style = MaterialTheme.typography.labelMedium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurface,
                                        selectedContainerColor = accent.copy(alpha = 0.22f),
                                        selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                                        selectedBorderColor = accent.copy(alpha = 0.65f),
                                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                        disabledSelectedBorderColor = accent.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowTags.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tags", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Create custom tags and choose which tags appear on the Home screen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTagInput,
                            onValueChange = { newTagInput = it },
                            label = { Text("New tag") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                onAddCustomTag(newTagInput, selectedPastelArgb)
                                newTagInput = ""
                            }
                        ) { Text("Add") }
                    }
                    Text(
                        "Color",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PastelColorPickerRow(
                        selectedArgb = selectedPastelArgb,
                        onSelect = { selectedPastelArgb = it }
                    )

                    HorizontalDivider()
                    Text("Shown on Home", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Keep at least one tag visible on Home.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    state.allTags.chunked(2).forEach { rowTags ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowTags.forEach { tag ->
                                val accent = TagPalette.colorFor(tag, state.customTagColors)
                                val visible = state.homeVisibleTags.contains(tag)
                                FilterChip(
                                    selected = visible,
                                    onClick = { onHomeVisibleTagToggle(tag, !visible) },
                                    label = { Text(tag, style = MaterialTheme.typography.labelMedium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurface,
                                        selectedContainerColor = accent.copy(alpha = 0.22f),
                                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = visible,
                                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                                        selectedBorderColor = accent.copy(alpha = 0.65f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowTags.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }

                    if (state.allTags.isNotEmpty()) {
                        Text("Custom tags", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Delete removes it from Settings/Home and notification pickers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val customOnly = state.allTags.filter { it !in com.example.cs501clockin.model.SessionTags.defaults }
                        if (customOnly.isEmpty()) {
                            Text("No custom tags yet.")
                        } else {
                            customOnly.forEach { tag ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tag)
                                    TextButton(onClick = { onDeleteCustomTag(tag) }) { Text("Delete") }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Location suggestions", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Suggest a tag on Home when you are near a saved place.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable location suggestions")
                        Switch(
                            checked = state.locationSuggestionsEnabled,
                            onCheckedChange = onLocationSuggestionsChanged
                        )
                    }
                }
            }
        }

        item {
            Text(
                "Saved locations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(state.savedLocations, key = { it.id }) { loc ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(loc.label, style = MaterialTheme.typography.titleSmall)
                    Text("Suggest: ${loc.suggestedTag}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Lat ${"%.5f".format(loc.latitude)}, Lon ${"%.5f".format(loc.longitude)} · ${loc.radiusMeters} m",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { onDeleteSavedLocation(loc.id) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    labelInput = ""
                    selectedTag = state.allTags.firstOrNull() ?: ""
                    radiusInput = "150"
                    showAddDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add current location")
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    labelInput = ""
                    selectedTag = state.allTags.firstOrNull() ?: ""
                    radiusInput = "150"
                    pickedLatLng = null
                    showMapDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pick location on map")
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add saved location") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = labelInput,
                        onValueChange = { labelInput = it },
                        label = { Text("Label") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenuBox(
                        expanded = tagMenuExpanded,
                        onExpandedChange = { tagMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedTag,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Suggested tag") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tagMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = tagMenuExpanded,
                            onDismissRequest = { tagMenuExpanded = false }
                        ) {
                            state.allTags.forEach { tag ->
                                DropdownMenuItem(
                                    text = { Text(tag) },
                                    onClick = {
                                        selectedTag = tag
                                        tagMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = radiusInput,
                        onValueChange = { radiusInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Radius (meters)") },
                        supportingText = { Text("25–2000, default 150") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val r = radiusInput.toIntOrNull() ?: 150
                        val picked = pickedLatLng
                        if (picked != null) {
                            onAddSavedLocationManual(labelInput, selectedTag, r, picked.latitude, picked.longitude)
                            pickedLatLng = null
                        } else {
                            onAddSavedLocation(labelInput, selectedTag, r)
                        }
                        showAddDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showMapDialog) {
        MapPickerDialog(
            title = "Pick location on map",
            onDismiss = { showMapDialog = false },
            onConfirm = { latLng ->
                pickedLatLng = latLng
                showMapDialog = false
                // Re-open details dialog prefilled.
                showAddDialog = true
            }
        )
    }
}

@Composable
private fun PastelColorPickerRow(
    selectedArgb: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PastelTagColors.CHOICES_ARGB.forEach { argb ->
            val selected = argb == selectedArgb
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(argb.argbToColor())
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                        },
                        shape = CircleShape
                    )
                    .clickable { onSelect(argb) }
            )
        }
    }
}
