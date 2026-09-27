package com.solvyx.ui.screens.chatbot

/**
 * Decide si un mensaje de texto libre se envía a la API o lo responde el árbol local.
 *
 * - Sin red → árbol.
 * - Tras un fallo de la API → árbol durante [esperaMs] (no hacer esperar al usuario
 *   el timeout de conexión en cada mensaje contra un servidor caído).
 * - "Reintentar" es una acción explícita del usuario: ignora la espera.
 *
 * [reloj] es monotónico (no cambia si el usuario ajusta la hora) e inyectable para tests.
 */
class ChatModePolicy(
    private val esperaMs: Long = ESPERA_TRAS_FALLO_MS,
    private val reloj: () -> Long = { System.nanoTime() / 1_000_000 }
) {

    private var esperaHasta: Long? = null

    val enEspera: Boolean
        get() = esperaHasta?.let { reloj() < it } ?: false

    fun debeUsarApi(conectado: Boolean, esReintento: Boolean = false): Boolean =
        conectado && (esReintento || !enEspera)

    fun registrarFallo() {
        esperaHasta = reloj() + esperaMs
    }

    /** La API respondió (o el servidor volvió a responder): se sale de la espera. */
    fun registrarExito() {
        esperaHasta = null
    }

    companion object {
        const val ESPERA_TRAS_FALLO_MS = 60_000L
    }
}
