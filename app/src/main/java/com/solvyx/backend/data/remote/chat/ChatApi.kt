package com.solvyx.backend.data.remote.chat

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Endpoints de la API del chatbot.
 *
 * Devuelven [Response] (no el DTO directo) para poder leer el cuerpo de error
 * de los 4xx/5xx sin que Retrofit lance `HttpException`. Los errores de red
 * (timeout, servidor apagado) sí llegan como `IOException`.
 */
interface ChatApi {

    @POST("api/chat/mensaje")
    suspend fun enviarMensaje(@Body body: MensajeRequestDto): Response<MensajeResponseDto>

    @POST("api/chat/cerrar-sesion")
    suspend fun cerrarSesion(@Body body: CerrarSesionRequestDto): Response<CerrarSesionResponseDto>

    @GET(".")
    suspend fun ping(): Response<Unit>
}
