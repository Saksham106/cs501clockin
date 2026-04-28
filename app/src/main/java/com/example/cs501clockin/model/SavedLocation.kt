package com.example.cs501clockin.model

data class SavedLocation(
    val id: Long,
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val suggestedTag: String,
    val radiusMeters: Int
)
