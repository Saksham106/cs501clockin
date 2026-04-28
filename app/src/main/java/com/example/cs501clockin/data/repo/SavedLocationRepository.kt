package com.example.cs501clockin.data.repo

import com.example.cs501clockin.data.db.SavedLocationDao
import com.example.cs501clockin.data.db.SavedLocationEntity
import com.example.cs501clockin.model.SavedLocation
import com.example.cs501clockin.ui.util.haversineMeters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavedLocationRepository(
    private val dao: SavedLocationDao
) {
    fun observeAll(): Flow<List<SavedLocation>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun insert(
        label: String,
        latitude: Double,
        longitude: Double,
        suggestedTag: String,
        radiusMeters: Int = 150
    ): Long {
        val entity = SavedLocationEntity(
            label = label,
            latitude = latitude,
            longitude = longitude,
            suggestedTag = suggestedTag,
            radiusMeters = radiusMeters
        )
        return dao.insert(entity)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    /**
     * Returns the nearest saved location whose [radiusMeters] contains [lat]/[lon], or null.
     */
    fun nearestWithinRadius(lat: Double, lon: Double, locations: List<SavedLocation>): SavedLocation? {
        return locations
            .map { loc -> loc to haversineMeters(lat, lon, loc.latitude, loc.longitude) }
            .filter { (loc, dist) -> dist <= loc.radiusMeters }
            .minByOrNull { (_, dist) -> dist }
            ?.first
    }
}

private fun SavedLocationEntity.toDomain(): SavedLocation =
    SavedLocation(
        id = id,
        label = label,
        latitude = latitude,
        longitude = longitude,
        suggestedTag = suggestedTag,
        radiusMeters = radiusMeters
    )
