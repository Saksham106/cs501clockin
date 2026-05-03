package com.example.cs501clockin

import android.Manifest
import android.content.pm.PackageManager
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cs501clockin.data.repo.UserPreferences
import com.example.cs501clockin.ui.onboarding.TabOnboardingDialog
import com.example.cs501clockin.ui.onboarding.WelcomeOnboardingDialog
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cs501clockin.ui.components.ClockInTopBar
import com.example.cs501clockin.ui.navigation.Routes
import com.example.cs501clockin.ui.screens.DashboardScreen
import com.example.cs501clockin.ui.screens.EditSessionScreen
import com.example.cs501clockin.ui.screens.HistoryScreen
import com.example.cs501clockin.ui.screens.HomeScreen
import com.example.cs501clockin.ui.screens.SettingsScreen
import com.example.cs501clockin.ui.theme.Cs501clockinTheme
import com.example.cs501clockin.viewmodel.EditSessionViewModel
import com.example.cs501clockin.viewmodel.EditSessionViewModelFactory
import com.example.cs501clockin.viewmodel.HistoryViewModel
import com.example.cs501clockin.viewmodel.HistoryViewModelFactory
import com.example.cs501clockin.viewmodel.HomeViewModel
import com.example.cs501clockin.viewmodel.HomeViewModelFactory
import com.example.cs501clockin.viewmodel.LocationViewModel
import com.example.cs501clockin.viewmodel.LocationViewModelFactory
import com.example.cs501clockin.viewmodel.SettingsViewModel
import com.example.cs501clockin.viewmodel.SettingsViewModelFactory
import com.example.cs501clockin.viewmodel.SuggestionsViewModel
import com.example.cs501clockin.viewmodel.SuggestionsViewModelFactory
import com.example.cs501clockin.notification.LocationSuggestionNotifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Cs501clockinTheme {
                ClockInRoot()
            }
        }
    }
}

@Composable
private fun ClockInRoot() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.Home
    val topBarTitle = when {
        currentRoute.startsWith(Routes.EditSessionBase) -> "Edit Session"
        currentRoute == Routes.Home -> "ClockIn"
        currentRoute == Routes.History -> "History"
        currentRoute == Routes.Dashboard -> "Dashboard"
        currentRoute == Routes.Settings -> "Settings"
        else -> "ClockIn"
    }
    val context = LocalContext.current
    val app = context.applicationContext as ClockInApp
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val prefs by app.userPreferencesRepository.data.collectAsStateWithLifecycle(
        initialValue = UserPreferences()
    )

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(app.activeSessionStore, app.userPreferencesRepository)
    )
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val locationViewModel: LocationViewModel = viewModel(
        factory = LocationViewModelFactory(app.locationRepository)
    )
    val locationUiState by locationViewModel.uiState.collectAsStateWithLifecycle()

    val suggestionsViewModel: SuggestionsViewModel = viewModel(
        factory = SuggestionsViewModelFactory(
            app.savedLocationRepository,
            app.userPreferencesRepository,
            app.activeSessionStore,
            locationViewModel.uiState
        )
    )
    val suggestionsUiState by suggestionsViewModel.uiState.collectAsStateWithLifecycle()
    var lastNotifiedSuggestionId by remember { mutableLongStateOf(-1L) }

    val requestLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            locationViewModel.refresh()
        }
    }

    val postNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* optional: could snackbar if denied */ }

    LaunchedEffect(Unit) {
        if (app.locationRepository.hasFineLocationPermission()) {
            locationViewModel.refresh()
        }
    }

    LaunchedEffect(suggestionsUiState.suggestion) {
        val s = suggestionsUiState.suggestion ?: return@LaunchedEffect
        if (s.savedLocationId == lastNotifiedSuggestionId) return@LaunchedEffect
        lastNotifiedSuggestionId = s.savedLocationId
        LocationSuggestionNotifier.notify(
            context = context,
            label = s.label,
            suggestedTag = s.suggestedTag
        )
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val historyViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(app.sessionRepository)
    )
    val sessions by historyViewModel.sessions.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = { ClockInTopBar(title = topBarTitle) },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                NavigationBar {
                    val items = listOf(
                        Routes.Home to "Home",
                        Routes.History to "History",
                        Routes.Dashboard to "Dashboard",
                        Routes.Settings to "Settings"
                    )
                    items.forEach { (route, label) ->
                        val selected = currentRoute == route
                        val imageVector = when (route) {
                            Routes.Home -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
                            Routes.History -> if (selected) Icons.Filled.ListAlt else Icons.Outlined.ListAlt
                            Routes.Dashboard -> if (selected) Icons.Filled.Dashboard else Icons.Outlined.Dashboard
                            Routes.Settings -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
                            else -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = imageVector, contentDescription = label) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.Home,
                modifier = Modifier.padding(innerPadding)
            ) {
            composable(Routes.Home) {
                HomeScreen(
                    tags = homeUiState.tags,
                    selectedTag = homeUiState.selectedTag,
                    activeSession = homeUiState.activeSession,
                    tagColorArgbByTag = homeUiState.tagColorArgbByTag,
                    onTagSelected = homeViewModel::onTagSelected,
                    onStart = homeViewModel::startSession,
                    onEnd = { homeViewModel.endSession() },
                    locationSuggestion = suggestionsUiState.suggestion,
                    onAcceptLocationSuggestion = { tag ->
                        homeViewModel.onTagSelected(tag)
                        homeViewModel.startSession()
                    },
                    onDismissLocationSuggestion = { suggestionsViewModel.dismissCurrentSuggestion() }
                )
            }

            composable(Routes.History) {
                HistoryScreen(
                    sessions = sessions,
                    tagColorArgbByTag = prefs.customTagColors,
                    onSessionClick = { session ->
                        navController.navigate(Routes.editSession(session.id))
                    }
                )
            }

            composable(Routes.Dashboard) {
                DashboardScreen(
                    sessions = sessions,
                    tagColorArgbByTag = prefs.customTagColors
                )
            }

            composable(Routes.Settings) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModelFactory(
                        context.applicationContext as Application,
                        app.userPreferencesRepository,
                        app.savedLocationRepository,
                        app.locationRepository
                    )
                )
                val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    settingsViewModel.events.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                SettingsScreen(
                    state = settingsState,
                    onNotificationsChanged = settingsViewModel::setNotificationsEnabled,
                    onLocationSuggestionsChanged = settingsViewModel::setLocationSuggestionsEnabled,
                    onNotificationQuickTagToggle = settingsViewModel::toggleNotificationQuickTag,
                    onHomeVisibleTagToggle = settingsViewModel::toggleHomeVisibleTag,
                    onAddCustomTag = settingsViewModel::addCustomTag,
                    onDeleteCustomTag = settingsViewModel::deleteCustomTag,
                    onAddSavedLocation = settingsViewModel::addSavedLocationFromCurrent,
                    onAddSavedLocationManual = settingsViewModel::addSavedLocationManual,
                    onDeleteSavedLocation = settingsViewModel::deleteSavedLocation
                )
            }

            composable(
                route = Routes.EditSession,
                arguments = listOf(navArgument(Routes.EditSessionArg) { type = NavType.LongType })
            ) { entry ->
                val sessionId = entry.arguments?.getLong(Routes.EditSessionArg) ?: -1L
                val editVm: EditSessionViewModel = viewModel(
                    factory = EditSessionViewModelFactory(sessionId, app.sessionRepository)
                )
                val session by editVm.session.collectAsStateWithLifecycle()
                EditSessionScreen(
                    session = session,
                    onSave = { updated ->
                        editVm.save(updated)
                        navController.popBackStack()
                    },
                    onDelete = {
                        editVm.delete()
                        navController.popBackStack()
                    }
                )
            }
        }
        }

        when {
            !prefs.onboardingWelcomeCompleted -> {
                WelcomeOnboardingDialog(
                    onDismiss = {
                        scope.launch {
                            app.userPreferencesRepository.setOnboardingWelcomeCompleted(true)
                        }
                    }
                )
            }

            prefs.onboardingWelcomeCompleted &&
                currentRoute == Routes.Home &&
                !prefs.onboardingTipHomeSeen -> {
                TabOnboardingDialog(
                    route = Routes.Home,
                    onDismiss = {
                        scope.launch {
                            app.userPreferencesRepository.setOnboardingTipHomeSeen(true)
                        }
                    }
                )
            }

            prefs.onboardingWelcomeCompleted &&
                currentRoute == Routes.History &&
                !prefs.onboardingTipHistorySeen -> {
                TabOnboardingDialog(
                    route = Routes.History,
                    onDismiss = {
                        scope.launch {
                            app.userPreferencesRepository.setOnboardingTipHistorySeen(true)
                        }
                    }
                )
            }

            prefs.onboardingWelcomeCompleted &&
                currentRoute == Routes.Dashboard &&
                !prefs.onboardingTipDashboardSeen -> {
                TabOnboardingDialog(
                    route = Routes.Dashboard,
                    onDismiss = {
                        scope.launch {
                            app.userPreferencesRepository.setOnboardingTipDashboardSeen(true)
                        }
                    }
                )
            }

            prefs.onboardingWelcomeCompleted &&
                currentRoute == Routes.Settings &&
                !prefs.onboardingTipSettingsSeen -> {
                TabOnboardingDialog(
                    route = Routes.Settings,
                    onDismiss = {
                        scope.launch {
                            app.userPreferencesRepository.setOnboardingTipSettingsSeen(true)
                        }
                    }
                )
            }
        }
    }
}
