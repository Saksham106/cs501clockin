package com.example.cs501clockin.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class LatLng(
    val latitude: Double,
    val longitude: Double
)

sealed interface LocationResult {
    data class Success(val latLng: LatLng) : LocationResult
    data object PermissionDenied : LocationResult
    data class Error(val message: String) : LocationResult
}

class LocationRepository(
    private val appContext: Context
) {
    fun hasFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(): LocationResult {
        if (!hasFineLocationPermission()) return LocationResult.PermissionDenied

        val client = LocationServices.getFusedLocationProviderClient(appContext)
        return suspendCancellableCoroutine { cont ->
            client.lastLocation
                .addOnSuccessListener { loc ->
                    if (loc == null) {
                        cont.resume(LocationResult.Error("No last known location available"))
                    } else {
                        cont.resume(
                            LocationResult.Success(
                                LatLng(latitude = loc.latitude, longitude = loc.longitude)
                            )
                        )
                    }
                }
                .addOnFailureListener { e ->
                    cont.resume(LocationResult.Error(e.message ?: "Location failure"))
                }
        }
    }
}

