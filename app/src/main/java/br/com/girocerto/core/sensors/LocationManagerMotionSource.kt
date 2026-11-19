package br.com.girocerto.core.sensors

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import br.com.girocerto.core.util.ClockProvider
import br.com.girocerto.domain.repository.MotionSample
import br.com.girocerto.domain.repository.MotionSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationManagerMotionSource(
    private val context: Context,
    private val clockProvider: ClockProvider
) : MotionSource {

    @SuppressLint("MissingPermission")
    override fun observeSpeedSamples(): Flow<MotionSample> = callbackFlow {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            close()
            return@callbackFlow
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            close()
            return@callbackFlow
        }

        val request = LocationRequestCompat.Builder(1_000L)
            .setQuality(LocationRequestCompat.QUALITY_HIGH_ACCURACY)
            .setMinUpdateDistanceMeters(0f)
            .setMinUpdateIntervalMillis(1_000L)
            .build()

        val listener = LocationListenerCompat { location ->
            val speedKmh = if (location.hasSpeed()) location.speed * 3.6f else 0f
            trySend(
                MotionSample(
                    speedKmh = speedKmh,
                    timestampMillis = clockProvider.nowEpochMillis(),
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            )
        }

        LocationManagerCompat.requestLocationUpdates(
            locationManager,
            LocationManager.GPS_PROVIDER,
            request,
            listener,
            Looper.getMainLooper()
        )

        awaitClose {
            LocationManagerCompat.removeUpdates(locationManager, listener)
        }
    }
}