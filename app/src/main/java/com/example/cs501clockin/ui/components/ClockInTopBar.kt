package com.example.cs501clockin.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockInTopBar(title: String = "ClockIn") {
    TopAppBar(
        title = { Text(title, color = MaterialTheme.colorScheme.primary) }
    )
}

