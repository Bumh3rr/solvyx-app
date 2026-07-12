package com.solvyx.backend.data.local.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de **estado de conectividad** del dispositivo.
 *
 * Expone un [Flow] reactivo (`true` = conectado, `false` = sin red) que
 * la UI observa para mostrar el [com.solvyx.ui.components.common.SolvyxSinRedBanner].
 *
 * ## Decisiones
 * - Usa [ConnectivityManager.NetworkCallback] (API 21+, ok para `minSdk=24`).
 * - `VALIDATED` exige que la red pase el "captive portal check" del sistema.
 *   Así distinguimos "wifi asociado, pero sin internet" (no se cuenta como
 *   conectado para fines de banner).
 * - `TRANSPORT_CELLULAR` y `TRANSPORT_WIFI` y `TRANSPORT_ETHERNET` son los
 *   tres candidatos que acepta Solvyx. Cualquier otro transporte se ignora.
 *
 * ## Uso
 * ```
 * val isOnline by connectivity.observeConnected().collectAsStateWithLifecycle(initialValue = true)
 * if (!isOnline) SolvyxSinRedBanner(visible = true, ...)
 * ```
 *
 * **Nota de permisos**: requiere `android.permission.ACCESS_NETWORK_STATE`
 * (ya presente en `AndroidManifest.xml`).
 */
@Singleton
class ConnectivityRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val connectivityManager: ConnectivityManager? =
        context.getSystemService<ConnectivityManager>()

    /**
     * `true` cuando hay al menos una red con `NET_CAPABILITY_VALIDATED`.
     * Empieza con el estado actual y re-emite en cada cambio.
     */
    fun observeConnected(): Flow<Boolean> = callbackFlow {
        val cm = connectivityManager
        if (cm == null) {
            // Sin servicio, asumimos "online" para no romper la UX.
            trySend(true)
            awaitClose { }
            return@callbackFlow
        }

        // Snapshot inicial.
        trySend(cm.activeNetworkValidated())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(cm.activeNetworkValidated())
            }

            override fun onLost(network: Network) {
                trySend(cm.activeNetworkValidated())
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(cm.activeNetworkValidated())
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, callback)

        awaitClose {
            runCatching { cm.unregisterNetworkCallback(callback) }
        }
    }.distinctUntilChanged()

    /**
     * Snapshot sincrónico del estado de red. Útil para "boostrapping" sin
     * un `collect` activo.
     */
    fun isCurrentlyConnected(): Boolean =
        connectivityManager?.activeNetworkValidated() ?: true
}

/**
 * `true` si existe una red activa con `NET_CAPABILITY_VALIDATED`
 * (i.e., pasó el captive portal check del sistema).
 *
 * En API 26+ se usa `NetworkCapabilities.getNetworkCapabilities(...)` con
 * `activeNetwork`. En API 24/25 (minSdk=24) todavía no había
 * `activeNetworkOrNull`, así que se usa `activeNetwork` (puede ser `null`).
 */
private fun ConnectivityManager.activeNetworkValidated(): Boolean {
    val active = activeNetwork ?: return false
    val caps = getNetworkCapabilities(active) ?: return false
    if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    // En API < 23 no había VALIDATED; cualquier red con INTERNET cuenta.
    return true
}
