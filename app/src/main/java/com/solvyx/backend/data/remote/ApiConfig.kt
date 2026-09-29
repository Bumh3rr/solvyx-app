package com.solvyx.backend.data.remote

/**
 * Configuración de la API del chatbot (Berto online).
 */
object ApiConfig {

    const val BASE_URL = "https://solvyx.animlune.tech/"

    /** Si el servidor no responde en este tiempo, se considera caído. */
    const val CONNECT_TIMEOUT_SECONDS = 10L

    /**
     * El servidor espera a DeepSeek hasta 30 s (+5 s de conexión) con un margen
     * para no cortar una respuesta que sí iba a llegar.
     */
    const val READ_TIMEOUT_SECONDS = 45L

    const val WRITE_TIMEOUT_SECONDS = 10L
}