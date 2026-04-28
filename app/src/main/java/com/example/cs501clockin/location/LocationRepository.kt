package com.example.cs501clockin.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult as GmsLocationResult
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
                            Priority.PRIORITY_HIGH_ACCURACY,
                            tokenSource.token
                        )
                            .addOnSuccessListener { current ->
                                if (current == null) {
                                    // Fallback: request a single update. This helps when Play Services disconnects
                                    // or when getCurrentLocation returns null on some devices.
                                    requestSingleUpdate(client, cont)
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
                                // Fallback: request a single update. Common error: Play services disconnected.
                                requestSingleUpdate(client, cont, e.message)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    requestSingleUpdate(client, cont, e.message)
                }
        }
    }

    private fun requestSingleUpdate(
        client: com.google.android.gms.location.FusedLocationProviderClient,
        cont: kotlinx.coroutines.CancellableContinuation<LocationResult>,
        priorErrorMessage: String? = null
    ) {
        if (cont.isCompleted) return

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            /* intervalMillis */ 0L
        )
            .setWaitForAccurateLocation(false)
            .setMaxUpdates(1)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: GmsLocationResult) {
                val loc = result.lastLocation
                if (!cont.isCompleted) {
                    if (loc != null) {
                        cont.resume(
                            LocationResult.Success(
                                LatLng(latitude = loc.latitude, longitude = loc.longitude)
                            )
                        )
                    } else {
                        cont.resume(LocationResult.Error(priorErrorMessage ?: "Unable to get current location"))
                    }
                }
                client.removeLocationUpdates(this)
            }
        }

        cont.invokeOnCancellation {
            client.removeLocationUpdates(callback)
        }

        runCatching {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        }.onFailure { e ->
            if (!cont.isCompleted) {
                cont.resume(LocationResult.Error(e.message ?: (priorErrorMessage ?: "Location failure")))
            }
        }
    }
}

