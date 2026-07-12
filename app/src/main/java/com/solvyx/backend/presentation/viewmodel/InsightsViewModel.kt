package com.solvyx.backend.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvyx.backend.insights.Insight
import com.solvyx.backend.insights.InsightsEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel del motor de insights.
 *
 * **Responsabilidad**: exponer el resultado de `InsightsEngine.evaluateNow`
 * a la UI a través de [state] y [currentInsight]. La UI (banner, sheet)
 * consume estos flows.
 *
 * **Decisiones**:
 * - El motor ya aplica debouncing. El VM solo refleja "no hay insights
 *   nuevos" si el motor devuelve lista vacía.
 * - Si el motor lanza una excepción, mostramos [InsightsUiState.Error]
 *   con un mensaje neutro (sin detalles técnicos, sin alarmismo).
 * - El copy del insight lo entrega `backend-content-curator`; este VM
 *   no redacta texto.
 *
 * **Por qué un sealed interface para el estado**: hace explícitas todas
 *   las transiciones posibles (Idle → Loading → SinInsightsNuevos |
 *   InsightsDisponibles | Error) y obliga a la UI a manejar cada caso.
 */
@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val engine: InsightsEngine
) : ViewModel() {

    private val _state = MutableStateFlow<InsightsUiState>(InsightsUiState.Idle)
    val state: StateFlow<InsightsUiState> = _state.asStateFlow()

    private val _currentInsight = MutableStateFlow<Insight?>(null)
    val currentInsight: StateFlow<Insight?> = _currentInsight.asStateFlow()

    /**
     * Evalúa el motor de insights y emite el resultado a la UI.
     *
     * @param userAcceptsMore si `true`, reduce el debounce a 24h
     *   (configuración "más insights" en Mi Perfil). Por defecto `false`.
     */
    fun evaluarAhora(userAcceptsMore: Boolean = false) {
        viewModelScope.launch {
            _state.value = InsightsUiState.Loading
            try {
                val insights = engine.evaluateNow(userAcceptsMore)
                if (insights.isEmpty()) {
                    _currentInsight.value = null
                    _state.value = InsightsUiState.SinInsightsNuevos
                } else {
                    _currentInsight.value = insights.first()
                    _state.value = InsightsUiState.InsightsDisponibles(insights)
                }
            } catch (e: Exception) {
                // Mensaje neutro, sin alarmismo. No exponemos el detalle
                // técnico al usuario: el motor nunca debe asustar.
                _currentInsight.value = null
                _state.value = InsightsUiState.Error(
                    mensaje = "No pudimos analizar tu proceso ahora."
                )
            }
        }
    }

    /**
     * Cierra el insight mostrado y vuelve al estado Idle.
     * Llamado por la UI cuando el usuario descarta el banner.
     */
    fun onDismiss() {
        _currentInsight.value = null
        _state.value = InsightsUiState.Idle
    }
}

/**
 * Estados posibles del flujo de insights. Sealed para que el compilador
 * obligue a la UI a manejar cada rama.
 */
sealed interface InsightsUiState {

    /** Estado inicial: aún no se ha solicitado evaluación. */
    object Idle : InsightsUiState

    /** El motor está evaluando reglas. */
    object Loading : InsightsUiState

    /**
     * El motor terminó y no hay insights que mostrar.
     * Puede ser porque el debounce bloqueó la emisión o porque
     * ninguna regla aplicó. El copy lo diseña content-curator.
     */
    object SinInsightsNuevos : InsightsUiState

    /**
     * El motor devolvió al menos un insight. [insights] está ordenado
     * por severidad descendente.
     */
    data class InsightsDisponibles(val insights: List<Insight>) : InsightsUiState

    /**
     * Ocurrió un error al evaluar. El mensaje está en español y es
     * neutro (no técnico, no culpabilizador).
     */
    data class Error(val mensaje: String) : InsightsUiState
}