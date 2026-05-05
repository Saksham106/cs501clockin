@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cs501clockin.ui.preview

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.cs501clockin.model.SavedLocation
import com.example.cs501clockin.model.Session
import com.example.cs501clockin.model.SessionTags
import com.example.cs501clockin.ui.screens.DashboardScreen
import com.example.cs501clockin.ui.screens.EditSessionScreen
import com.example.cs501clockin.ui.screens.HistoryScreen
import com.example.cs501clockin.ui.screens.HomeScreen
import com.example.cs501clockin.ui.screens.SettingsScreen
import com.example.cs501clockin.ui.theme.Cs501clockinTheme
import com.example.cs501clockin.viewmodel.LocationSuggestion
import com.example.cs501clockin.viewmodel.SettingsUiState

private fun previewSessionsForToday(): List<Session> {
    val now = System.currentTimeMillis()
    return listOf(
        Session(
            id = 1L,
            tag = SessionTags.WORK,
            startTimeMillis = now - 3_600_000L,
            endTimeMillis = now - 1_800_000L,
            notes = "Deep work",
            edited = false
        ),
        Session(
            id = 2L,
            tag = SessionTags.SCHOOL,
            startTimeMillis = now - 900_000L,
            endTimeMillis = now - 120_000L,
            notes = null,
            edited = false
        )
    )
}

@Preview(showBackground = true, name = "Home — idle")
@Composable
private fun PreviewHomeIdle() {
    Cs501clockinTheme(darkTheme = false) {
        val now = System.currentTimeMillis()
        HomeScreen(
            tags = SessionTags.defaults,
            selectedTag = SessionTags.IDLE,
            activeSession = Session(
                id = now,
                tag = SessionTags.IDLE,
                startTimeMillis = now
            ),
            onTagSelected = {},
            onStart = {},
            onEnd = {}
        )
    }
}

@Preview(showBackground = true, name = "Home — suggestion")
@Composable
private fun PreviewHomeSuggestion() {
    Cs501clockinTheme(darkTheme = false) {
        val now = System.currentTimeMillis()
        HomeScreen(
            tags = listOf(SessionTags.WORK, SessionTags.SCHOOL),
            selectedTag = SessionTags.WORK,
            activeSession = Session(
                id = now,
                tag = SessionTags.IDLE,
                startTimeMillis = now - 60_000L
            ),
            onTagSelected = {},
            onStart = {},
            onEnd = {},
            locationSuggestion = LocationSuggestion(
                savedLocationId = 1L,
                label = "Library",
                suggestedTag = SessionTags.SCHOOL
            ),
            onAcceptLocationSuggestion = {},
            onDismissLocationSuggestion = {}
        )
    }
}

@Preview(showBackground = true, name = "History")
@Composable
private fun PreviewHistory() {
    Cs501clockinTheme(darkTheme = false) {
        HistoryScreen(
            sessions = previewSessionsForToday(),
            onSessionClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Dashboard")
@Composable
private fun PreviewDashboard() {
    Cs501clockinTheme(darkTheme = false) {
        DashboardScreen(sessions = previewSessionsForToday())
    }
}

@Preview(showBackground = true, name = "Edit session")
@Composable
private fun PreviewEditSession() {
    Cs501clockinTheme(darkTheme = false) {
        val now = System.currentTimeMillis()
        EditSessionScreen(
            session = Session(
                id = 42L,
                tag = SessionTags.TRAINING,
                startTimeMillis = now - 7_200_000L,
                endTimeMillis = now - 3_600_000L,
                notes = "Leg day",
                edited = false
            ),
            onSave = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true, name = "Settings")
@Composable
private fun PreviewSettings() {
    Cs501clockinTheme(darkTheme = false) {
        SettingsScreen(
            state = SettingsUiState(
                notificationsEnabled = true,
                locationSuggestionsEnabled = true,
                allTags = SessionTags.defaults,
                homeVisibleTags = SessionTags.defaults.take(4).toSet(),
                notificationQuickTags = SessionTags.defaults.take(3).toSet(),
                savedLocations = listOf(
                    SavedLocation(
                        id = 1L,
                        label = "Campus gym",
                        latitude = 42.36,
                        longitude = -71.06,
                        suggestedTag = SessionTags.TRAINING,
                        radiusMeters = 150
                    )
                )
            ),
            onNotificationsChanged = {},
            onLocationSuggestionsChanged = {},
            onCalendarSuggestionsChanged = {},
            onNotificationQuickTagToggle = { _, _ -> },
            onHomeVisibleTagToggle = { _, _ -> },
            onAddCustomTag = { _, _ -> },
            onDeleteCustomTag = {},
            onAddCalendarTagRule = { _, _ -> },
            onDeleteCalendarTagRule = { _, _ -> },
            onSeedSampleData = {},
            onResetOnboarding = {},
            showDeveloperTools = true,
            onAddSavedLocation = { _, _, _ -> },
            onAddSavedLocationManual = { _, _, _, _, _ -> },
            onDeleteSavedLocation = {}
        )
    }
}
