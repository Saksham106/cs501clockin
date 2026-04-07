package com.example.cs501clockin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.cs501clockin.viewmodel.HistoryViewModel
import com.example.cs501clockin.viewmodel.HistoryViewModelFactory
import com.example.cs501clockin.viewmodel.EditSessionViewModel
import com.example.cs501clockin.viewmodel.EditSessionViewModelFactory
import com.example.cs501clockin.viewmodel.HomeViewModel
import com.example.cs501clockin.viewmodel.HomeViewModelFactory
import com.example.cs501clockin.viewmodel.LocationViewModel
import com.example.cs501clockin.viewmodel.LocationViewModelFactory
import com.example.cs501clockin.viewmodel.WeatherViewModel
import com.example.cs501clockin.viewmodel.WeatherViewModelFactory
import com.example.cs501clockin.location.LocationResult

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
    val context = LocalContext.current
    val app = context.applicationContext as ClockInApp
    val snackbarHostState = remember { SnackbarHostState() }
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(app.sessionRepository)
    )
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val locationViewModel: LocationViewModel = viewModel(
        factory = LocationViewModelFactory(app.locationRepository)
    )
    val locationUiState by locationViewModel.uiState.collectAsStateWithLifecycle()
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            locationViewModel.refresh()
        }
    }
    val historyViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(app.sessionRepository)
    )
    val sessions by historyViewModel.sessions.collectAsStateWithLifecycle()

    val weatherViewModel: WeatherViewModel = viewModel(
        factory = WeatherViewModelFactory(app.weatherRepository)
    )
    val weatherUiState by weatherViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (app.locationRepository.hasFineLocationPermission()) {
            locationViewModel.refresh()
        }
    }

    LaunchedEffect(locationUiState.result) {
        val result = locationUiState.result
        if (result is LocationResult.Success) {
            weatherViewModel.refresh(
                latitude = result.latLng.latitude,
                longitude = result.latLng.longitude
            )
        }
    }

    Scaffold(
        topBar = { ClockInTopBar() },
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
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {},
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(Routes.Home) {
                HomeScreen(
                    tags = homeUiState.tags,
                    selectedTag = homeUiState.selectedTag,
                    activeSession = homeUiState.activeSession,
                    onTagSelected = homeViewModel::onTagSelected,
                    onStart = homeViewModel::startSession,
                    onEnd = { homeViewModel.endSession() },
                    locationState = locationUiState,
                    onRequestLocationPermission = {
                        requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    onRefreshLocation = { locationViewModel.refresh() },
                    weatherState = weatherUiState
                )
            }

            composable(Routes.History) {
                HistoryScreen(
                    sessions = sessions,
                    onSessionClick = { session ->
                        navController.navigate(Routes.editSession(session.id))
                    }
                )
            }

            composable(Routes.Dashboard) {
                DashboardScreen(sessions = sessions)
            }

            composable(Routes.Settings) {
                SettingsScreen()
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
}