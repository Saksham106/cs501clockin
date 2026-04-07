package com.example.cs501clockin.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
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
                    if (loc != null) {
                        cont.resume(
                            LocationResult.Success(
                                LatLng(latitude = loc.latitude, longitude = loc.longitude)
                            )
                        )
                    } else {
                        val tokenSource = CancellationTokenSource()
                        cont.invokeOnCancellation { tokenSource.cancel() }

                        client.getCurrentLocation(
                            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                            tokenSource.token
                        )
                            .addOnSuccessListener { current ->
                                if (current == null) {
                                    cont.resume(LocationResult.Error("Unable to get current location"))
                                } else {
                                    cont.resume(
                                        LocationResult.Success(
                                            LatLng(
                                                latitude = current.latitude,
                                                longitude = current.longitude
                                            )
                                        )
                                    )
                                }
                            }
                            .addOnFailureListener { e ->
                                cont.resume(LocationResult.Error(e.message ?: "Location failure"))
                            }
                    }
                }
                .addOnFailureListener { e ->
                    cont.resume(LocationResult.Error(e.message ?: "Location failure"))
                }
        }
    }
}

