package com.solvyx.backend.data.remote.chat

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException
import java.text.Normalizer
import javax.inject.Inject
import javax.inject.Singleton

/** Firebase UID plus an ID token that proves it; the server verifies the token before trusting the UID. */
data class ChatCredentials(val userId: String, val idToken: String)

/** Source of the Firebase credentials sent to the API. An interface so it can be tested without Firebase. */
fun interface ChatCredentialsProvider {
    /** `null` when there is no Firebase user. Throws if the token cannot be obtained (e.g. no network). */
    suspend fun currentCredentials(): ChatCredentials?
}

/**
 * Acceso a la API del chatbot.
 *
 * Traduce cada respuesta a [EnviarMensajeResult] / [CerrarSesionResult] para
 * que el ViewModel no tenga que conocer códigos HTTP ni excepciones de red.
 *
 * La API no expone códigos de error propios: los 400 se distinguen por el
 * texto de `mensaje`, comparado sin acentos ni mayúsculas (igual que la API).
 */
@Singleton
class ChatRemoteRepository @Inject constructor(
    private val chatApi: ChatApi,
    private val credentialsProvider: ChatCredentialsProvider
) {

    private val gson = Gson()

    suspend fun enviarMensaje(mensaje: String): EnviarMensajeResult {
        val texto = mensaje.trim()
        if (texto.isEmpty() || texto.length > MAX_CARACTERES) return EnviarMensajeResult.MensajeInvalido
        val credentials = when (val lookup = lookupCredentials()) {
            CredentialsLookup.NoUser -> return EnviarMensajeResult.SinSesion
            CredentialsLookup.Unavailable -> return EnviarMensajeResult.ServidorNoDisponible
            is CredentialsLookup.Found -> lookup.credentials
        }

        return try {
            val body = MensajeRequestDto(credentials.userId, NOMBRE, texto)
            mapearMensaje(chatApi.enviarMensaje(credentials.bearer(), body))
        } catch (e: CancellationException) {
            throw e
        } catch (e: MalformedJsonException) {
            EnviarMensajeResult.ErrorInesperado
        } catch (e: JsonParseException) {
            EnviarMensajeResult.ErrorInesperado
        } catch (e: IOException) {
            EnviarMensajeResult.ServidorNoDisponible
        } catch (e: RuntimeException) {
            EnviarMensajeResult.ErrorInesperado
        }
    }

    suspend fun cerrarSesion(): CerrarSesionResult {
        val credentials = when (val lookup = lookupCredentials()) {
            CredentialsLookup.NoUser -> return CerrarSesionResult.SinSesionActiva
            CredentialsLookup.Unavailable -> return CerrarSesionResult.Fallo
            is CredentialsLookup.Found -> lookup.credentials
        }

        return try {
            val response = chatApi.cerrarSesion(credentials.bearer(), CerrarSesionRequestDto(credentials.userId))
            when {
                response.isSuccessful -> CerrarSesionResult.Cerrada(response.body()?.historialId)
                response.code() == 404 -> CerrarSesionResult.SinSesionActiva
                else -> CerrarSesionResult.Fallo
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            CerrarSesionResult.Fallo
        }
    }

    /**
     * `true` si el servidor responde algo (cualquier código HTTP); `false` si no hay respuesta
     * (apagado, sin ruta, timeout). No garantiza que DeepSeek funcione.
     */
    suspend fun servidorDisponible(): Boolean =
        try {
            chatApi.ping()
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            false
        }

    private suspend fun lookupCredentials(): CredentialsLookup =
        try {
            credentialsProvider.currentCredentials()
                ?.let { CredentialsLookup.Found(it) }
                ?: CredentialsLookup.NoUser
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Firebase could not refresh the token (usually no network): same as the server being down.
            CredentialsLookup.Unavailable
        }

    private fun ChatCredentials.bearer(): String = "$BEARER_PREFIX$idToken"

    private sealed interface CredentialsLookup {
        data class Found(val credentials: ChatCredentials) : CredentialsLookup
        data object NoUser : CredentialsLookup
        data object Unavailable : CredentialsLookup
    }

    private fun mapearMensaje(response: Response<MensajeResponseDto>): EnviarMensajeResult {
        if (response.isSuccessful) {
            val body = response.body()
            val respuesta = body?.respuesta
            return if (respuesta.isNullOrBlank()) {
                EnviarMensajeResult.RespuestaVacia
            } else {
                EnviarMensajeResult.Exito(respuesta, body?.historialId)
            }
        }

        return when (response.code()) {
            in 500..599 -> EnviarMensajeResult.ServidorNoDisponible
            400 -> {
                val mensajeError = normalizar(leerMensajeError(response))
                when {
                    ERROR_CONTENIDO_NO_PERMITIDO in mensajeError -> EnviarMensajeResult.ContenidoNoPermitido
                    ERROR_RESPUESTA_VACIA in mensajeError -> EnviarMensajeResult.RespuestaVacia
                    else -> EnviarMensajeResult.ErrorInesperado
                }
            }
            else -> EnviarMensajeResult.ErrorInesperado
        }
    }

    private fun leerMensajeError(response: Response<*>): String =
        runCatching {
            gson.fromJson(response.errorBody()?.string(), ApiErrorDto::class.java)?.mensaje
        }.getOrNull().orEmpty()

    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(DIACRITICOS, "")
            .lowercase()

    companion object {
        /** Límite de la API (`@Size` del DTO y `ValidacionMensajeService`). */
        const val MAX_CARACTERES = 600

        /** La API guarda el nombre pero no lo usa en el prompt; no mandamos datos personales. */
        const val NOMBRE = "Usuario"

        private const val BEARER_PREFIX = "Bearer "
        private const val ERROR_CONTENIDO_NO_PERMITIDO = "contenido no permitido"
        private const val ERROR_RESPUESTA_VACIA = "respuesta generada esta vacia"
        private val DIACRITICOS = Regex("\\p{Mn}+")
    }
}
