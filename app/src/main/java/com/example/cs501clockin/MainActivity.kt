package com.example.cs501clockin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.cs501clockin.ui.theme.Cs501clockinTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Cs501clockinTheme {
                ClockInSimpleApp()
            }
        }
    }
}

private enum class Tab(val route: String, val label: String) {
    HOME("home", "Home"),
    HISTORY("history", "History"),
    EDIT("edit", "Edit")
}

private data class Session(
    val id: Long,
    val tag: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long? = null
)

private data class ClockInUiState(
    val tags: List<String> = listOf("Idle", "Study", "Class", "Gym", "Work", "Errands"),
    val selectedTag: String = "Idle",
    val activeSession: Session? = null,
    val sessions: List<Session> = listOf(
        Session(
            id = 1,
            tag = "Class",
            startTimeMillis = System.currentTimeMillis() - 150 * 60_000L,
            endTimeMillis = System.currentTimeMillis() - 95 * 60_000L
        ),
        Session(
            id = 2,
            tag = "Gym",
            startTimeMillis = System.currentTimeMillis() - 90 * 60_000L,
            endTimeMillis = System.currentTimeMillis() - 40 * 60_000L
        )
    )
)

private class ClockInViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ClockInUiState())
    val uiState: StateFlow<ClockInUiState> = _uiState.asStateFlow()

    fun onTagSelected(tag: String) {
        _uiState.value = _uiState.value.copy(selectedTag = tag)
    }

    fun startSession() {
        val current = _uiState.value
        if (current.selectedTag == "Idle") return

        _uiState.value = current.copy(
            activeSession = Session(
                id = System.currentTimeMillis(),
                tag = current.selectedTag,
                startTimeMillis = System.currentTimeMillis()
            )
        )
    }

    fun endSession() {
        val current = _uiState.value
        val active = current.activeSession ?: return
        val now = System.currentTimeMillis()
        val completed = active.copy(endTimeMillis = now)

        _uiState.value = current.copy(
            sessions = listOf(completed) + current.sessions,
            activeSession = null,
            selectedTag = "Idle"
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ClockInSimpleApp(viewModel: ClockInViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Tab.HOME.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ClockIn", color = MaterialTheme.colorScheme.primary) }
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {},
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.HOME.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Tab.HOME.route) {
                HomeScreen(
                    tags = uiState.tags,
                    selectedTag = uiState.selectedTag,
                    activeSession = uiState.activeSession,
                    onTagSelected = viewModel::onTagSelected,
                    onStart = viewModel::startSession,
                    onEnd = viewModel::endSession
                )
            }

            composable(Tab.HISTORY.route) {
                HistoryScreen(sessions = uiState.sessions)
            }

            composable(Tab.EDIT.route) {
                EditScreen()
            }
        }
    }
}

@Composable
private fun HomeScreen(
    tags: List<String>,
    selectedTag: String,
    activeSession: Session?,
    onTagSelected: (String) -> Unit,
    onStart: () -> Unit,
    onEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
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
            enabled = selectedTag != "Idle",
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (selectedTag == "Idle") "Select a task tag" else "Start ${selectedTag} Session")
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

        if (activeSession == null && selectedTag == "Idle") {
            Text(
                text = "Status: Idle",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun HistoryScreen(
    sessions: List<Session>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(sessions, key = { it.id }) { session ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(session.tag, style = MaterialTheme.typography.titleMedium)
                    Text("${formatClockTime(session.startTimeMillis)} - ${session.endTimeMillis?.let(::formatClockTime) ?: "Now"}")
                    Text("Duration: ${formatDuration(session.startTimeMillis, session.endTimeMillis)}")
                }
            }
        }
    }
}

@Composable
private fun EditScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Edit Session Page")
        Text("ToDo later")
    }
}

private fun formatClockTime(epochMillis: Long): String {
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(Date(epochMillis))
}

private fun formatDuration(start: Long, end: Long?): String {
    val endValue = end ?: System.currentTimeMillis()
    val minutes = ((endValue - start) / 60_000L).coerceAtLeast(0L)
    val hours = minutes / 60L
    val remain = minutes % 60L
    return if (hours > 0) "${hours}h ${remain}m" else "${minutes}m"
}