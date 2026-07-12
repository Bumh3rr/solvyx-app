package com.solvyx.backend.data.local.connectivity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solvyx.ui.components.common.SolvyxSinRedBanner
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint que expone [ConnectivityRepository] para consumo desde
 * Composables que **no** quieren ser `@HiltViewModel` (típicamente el
 * shell `MainScreen`).
 *
 * ## Por qué un EntryPoint
 * El shell gráfico vive encima del grafo de navegación y NO queremos
 * sobrecargarlo con VMs adicionales sólo para exponer un `Flow<Boolean>`.
 * [EntryPointAccessors] es el patrón oficial de Hilt para esto.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ConnectivityEntryPoint {
    fun connectivityRepository(): ConnectivityRepository
}

/**
 * `true` cuando el dispositivo tiene red validada. Útil para que el shell
 * (`MainScreen`) muestre el [SolvyxSinRedBanner] de forma reactiva.
 */
@Composable
fun SolvyxConnectivityState(): Boolean {
    val context = LocalContext.current
    val ep = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ConnectivityEntryPoint::class.java
        )
    }
    val repo = remember(ep) { ep.connectivityRepository() }
    val isOnline by repo.observeConnected()
        .collectAsStateWithLifecycle(initialValue = true)
    return isOnline
}

/**
 * Banner global "sin red". Píntalo arriba del contenido principal.
 * Detrás del banner va el contenido (no tapa la UI; sólo avisa).
 */
@Composable
fun SolvyxOfflineBanner(
    modifier: Modifier = Modifier
) {
    val isOnline = SolvyxConnectivityState()
    SolvyxSinRedBanner(
        visible = !isOnline,
        onAction = { /* Por ahora solo informativo */ },
        modifier = modifier
    )
}
