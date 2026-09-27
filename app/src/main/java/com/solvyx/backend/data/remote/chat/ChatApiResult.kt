package com.solvyx.backend.data.remote.chat

/**
 * Resultado de enviar un mensaje a la API. [ChatRemoteRepository] traduce
 * cualquier respuesta HTTP o fallo de red a uno de estos casos; nunca lanza.
 */
sealed interface EnviarMensajeResult {

    data class Exito(val respuesta: String, val historialId: Long?) : EnviarMensajeResult

    /** 400 por `PalabraProhibida` (en el mensaje del usuario o en la respuesta de DeepSeek). */
    data object ContenidoNoPermitido : EnviarMensajeResult

    /** DeepSeek no generó texto (400 de la API, o 200 sin `respuesta`). */
    data object RespuestaVacia : EnviarMensajeResult

    /** 5xx, timeout, servidor apagado o conexión cortada: el chat cae al árbol local. */
    data object ServidorNoDisponible : EnviarMensajeResult

    /** No hay usuario de Firebase, por lo tanto no hay `userId`: se trata como offline. */
    data object SinSesion : EnviarMensajeResult

    /** Mensaje vacío o de más de [ChatRemoteRepository.MAX_CARACTERES]; no se envía. */
    data object MensajeInvalido : EnviarMensajeResult

    /** Cualquier otra respuesta (400 de validación, JSON inválido, código no previsto). */
    data object ErrorInesperado : EnviarMensajeResult
}

/** Resultado de cerrar la sesión en la API. Se usa en segundo plano; los fallos se ignoran. */
sealed interface CerrarSesionResult {

    data class Cerrada(val historialId: Long?) : CerrarSesionResult

    /** 404: no había sesión activa (ya cerrada o nunca abierta). Es un caso normal. */
    data object SinSesionActiva : CerrarSesionResult

    data object Fallo : CerrarSesionResult
}
