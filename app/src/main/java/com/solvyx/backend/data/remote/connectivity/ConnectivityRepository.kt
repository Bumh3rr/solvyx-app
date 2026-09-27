package com.solvyx.backend.data.remote.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Estado de conectividad del dispositivo (`true` = hay internet).
 *
 * Solo cuenta como conectada una red con `NET_CAPABILITY_VALIDATED`, es decir,
 * que pasó el chequeo del sistema: conectado a Wi-Fi pero sin salida a
 * internet cuenta como desconectado.
 *
 * Esto no garantiza que el servidor de la API esté arriba; eso solo se sabe
 * al fallar una petición.
 */
@Singleton
class ConnectivityRepository @Inject constructor(
    @ApplicationContext context: Context
) {

    private val connectivityManager: ConnectivityManager? = context.getSystemService()

    /** Emite el estado actual y re-emite en cada cambio de la red por defecto. */
    fun observeConnected(): Flow<Boolean> = callbackFlow {
        val cm = connectivityManager
        if (cm == null) {
            // Sin servicio de conectividad asumimos online; si la API falla, el chat cae al árbol.
            trySend(true)
            awaitClose { }
            return@callbackFlow
        }

        trySend(cm.isActiveNetworkValidated())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(caps.isValidatedInternet())
            }

            // Al cambiar de Wi-Fi a datos se pierde la red anterior; se consulta la activa
            // para no emitir un "desconectado" momentáneo.
            override fun onLost(network: Network) {
                trySend(cm.isActiveNetworkValidated())
            }
        }
        cm.registerDefaultNetworkCallback(callback)

        awaitClose { runCatching { cm.unregisterNetworkCallback(callback) } }
    }.distinctUntilChanged()

    /** Snapshot sincrónico, para decidir el modo al momento de enviar un mensaje. */
    fun isCurrentlyConnected(): Boolean = connectivityManager?.isActiveNetworkValidated() ?: true
}

private fun ConnectivityManager.isActiveNetworkValidated(): Boolean {
    val network = activeNetwork ?: return false
    return getNetworkCapabilities(network)?.isValidatedInternet() ?: false
}

private fun NetworkCapabilities.isValidatedInternet(): Boolean =
    hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
