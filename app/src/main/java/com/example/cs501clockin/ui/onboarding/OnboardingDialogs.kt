package com.example.cs501clockin.ui.onboarding

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.cs501clockin.R
import com.example.cs501clockin.ui.navigation.Routes

@Composable
fun WelcomeOnboardingDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.onboarding_welcome_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(R.string.onboarding_welcome_body),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 4.dp)
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.onboarding_got_it))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.onboarding_skip))
            }
        }
    )
}

@Composable
fun TabOnboardingDialog(
    route: String,
    onDismiss: () -> Unit
) {
    val title = when (route) {
        Routes.Home -> stringResource(R.string.onboarding_tip_home_title)
        Routes.History -> stringResource(R.string.onboarding_tip_history_title)
        Routes.Dashboard -> stringResource(R.string.onboarding_tip_dashboard_title)
        Routes.Settings -> stringResource(R.string.onboarding_tip_settings_title)
        else -> return
    }
    val body = when (route) {
        Routes.Home -> stringResource(R.string.onboarding_tip_home_body)
        Routes.History -> stringResource(R.string.onboarding_tip_history_body)
        Routes.Dashboard -> stringResource(R.string.onboarding_tip_dashboard_body)
        Routes.Settings -> stringResource(R.string.onboarding_tip_settings_body)
        else -> return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Text(body, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.onboarding_got_it))
            }
        }
    )
}
