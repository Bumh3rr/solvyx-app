package com.solvyx.backend.data.remote.chat

import com.google.gson.annotations.SerializedName
data class MensajeRequestDto(
    @SerializedName("userId") val userId: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("mensaje") val mensaje: String
)

data class MensajeResponseDto(
    @SerializedName("respuesta") val respuesta: String? = null,
    @SerializedName("historialId") val historialId: Long? = null
)

data class CerrarSesionRequestDto(
    @SerializedName("userId") val userId: String
)

data class CerrarSesionResponseDto(
    @SerializedName("historialId") val historialId: Long? = null,
    @SerializedName("status") val status: String? = null
)

/** Cuerpo de error común de la API (`GlobalExceptionHandler`). */
data class ApiErrorDto(
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("status") val status: Int? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("mensaje") val mensaje: String? = null
)
