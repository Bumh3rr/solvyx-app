package com.solvyx.backend.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.solvyx.backend.sms.Coordinates
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** How long the SOS waits for a fix: the alert must not be held back for long. */
private const val FIX_TIMEOUT_MS = 5_000L
private val MAX_CACHED_FIX_AGE_MS = TimeUnit.MINUTES.toMillis(2)
/** Older than this, a last-known position could send contacts to the wrong place. */
private val MAX_FALLBACK_FIX_AGE_MS = TimeUnit.MINUTES.toMillis(10)

fun Context.hasLocationPermission(): Boolean =
    listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

/** The user's position for the SOS text. Only asked for at the moment SOS is pressed. */
@Singleton
class SosLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    /** A fresh fix, else a recent last-known one; null without permission, with location off or too slow. */
    suspend fun currentLocation(): Coordinates? {
        if (!context.hasLocationPermission()) return null
        return try {
            (freshFix() ?: recentLastKnownFix())?.let { Coordinates(it.latitude, it.longitude) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // SecurityException (permission revoked meanwhile) or Play services unavailable:
            // the SOS still goes out, just without the link.
            null
        }
    }

    @Suppress("MissingPermission") // checked in currentLocation()
    private suspend fun freshFix(): Location? {
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(MAX_CACHED_FIX_AGE_MS)
            .setDurationMillis(FIX_TIMEOUT_MS)
            .build()
        val cancellation = CancellationTokenSource()
        return withTimeoutOrNull(FIX_TIMEOUT_MS) {
            client.getCurrentLocation(request, cancellation.token).await(cancellation)
        }
    }

    @Suppress("MissingPermission") // checked in currentLocation()
    private suspend fun recentLastKnownFix(): Location? =
        client.lastLocation.await()?.takeIf { it.ageMillis() <= MAX_FALLBACK_FIX_AGE_MS }

    private fun Location.ageMillis(): Long =
        TimeUnit.NANOSECONDS.toMillis(SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos)
}
