package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper

data class GpsCoordinate(
    val latitude: Double = 38.5449,
    val longitude: Double = -121.7405,
    val altitude: Double = 16.0,
    val accuracyMeters: Float = 3.2f,
    val isGpsLocked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class LocationHelper(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): GpsCoordinate {
        if (locationManager == null) return GpsCoordinate()

        try {
            val gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val bestLoc = gpsLoc ?: netLoc

            if (bestLoc != null) {
                return GpsCoordinate(
                    latitude = bestLoc.latitude,
                    longitude = bestLoc.longitude,
                    altitude = bestLoc.altitude,
                    accuracyMeters = bestLoc.accuracy,
                    isGpsLocked = true,
                    timestamp = bestLoc.time
                )
            }
        } catch (_: SecurityException) {
            // Permission not yet granted
        }

        // Return standard agricultural test location (e.g., UC Davis Agronomy Research Farm Coordinates)
        return GpsCoordinate(
            latitude = 38.5449,
            longitude = -121.7405,
            altitude = 16.0,
            accuracyMeters = 5.0f,
            isGpsLocked = false
        )
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdate(onLocationResult: (GpsCoordinate) -> Unit) {
        if (locationManager == null) {
            onLocationResult(getLastKnownLocation())
            return
        }

        try {
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    onLocationResult(
                        GpsCoordinate(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            altitude = location.altitude,
                            accuracyMeters = location.accuracy,
                            isGpsLocked = true,
                            timestamp = location.time
                        )
                    )
                }
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, Looper.getMainLooper())
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, Looper.getMainLooper())
            } else {
                onLocationResult(getLastKnownLocation())
            }
        } catch (_: SecurityException) {
            onLocationResult(getLastKnownLocation())
        } catch (_: Exception) {
            onLocationResult(getLastKnownLocation())
        }
    }
}
