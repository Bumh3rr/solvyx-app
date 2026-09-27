package com.solvyx.backend.data.remote.chat

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Endpoints de la API del chatbot.
 *
 * Devuelven [Response] (no el DTO directo) para poder leer el cuerpo de error
 * de los 4xx/5xx sin que Retrofit lance `HttpException`. Los errores de red
 * (timeout, servidor apagado) sí llegan como `IOException`.
 *
 * Los endpoints de sesión llevan el ID token de Firebase (`Authorization: Bearer <token>`)
 * para que el servidor verifique que el `userId` del cuerpo es de quien hace la petición.
 */
interface ChatApi {

    @POST("api/chat/mensaje")
    suspend fun enviarMensaje(
        @Header(AUTHORIZATION_HEADER) authorization: String,
        @Body body: MensajeRequestDto
    ): Response<MensajeResponseDto>

    @POST("api/chat/cerrar-sesion")
    suspend fun cerrarSesion(
        @Header(AUTHORIZATION_HEADER) authorization: String,
        @Body body: CerrarSesionRequestDto
    ): Response<CerrarSesionResponseDto>

    @GET(".")
    suspend fun ping(): Response<Unit>

    companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
    }
}
