package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Ejercicio de regulación (respiración, grounding, body-scan, activación, lugar seguro).
 *
 * Dominio: catálogo de ejercicios TTS-guiados disponibles offline.
 *
 * Campos JSON (parseados en repositorio con Gson):
 * - [pasos]: array JSON de strings con la lista ordenada de instrucciones paso a paso.
 *   Ejemplo: `["Inhala 4 segundos", "Sostén 4", "Exhala 6"]`.
 * - [ttsText]: objeto JSON con el texto a leer por TTS en cada idioma disponible.
 *   Las claves son códigos BCP-47 (`es-MX`, `en-US`, ...).
 *   Ejemplo: `{"es-MX":"Inhala profundo...", "en-US":"Breathe in deeply..."}`.
 *
 * Los ejercicios son contenido versionado: nunca se borran físicamente,
 * se desactivan con [activo] = false para mantener historial de uso.
 */
@Entity(
    tableName = "ejercicios",
    indices = [
        Index(value = ["slug"], unique = true),
        Index(value = ["tipo"], name = "idx_ejercicios_tipo"),
        Index(value = ["orden"], name = "idx_ejercicios_orden")
    ]
)
data class EjercicioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val slug: String,
    val nombre: String,
    val tipo: String,
    val duracionMinutos: Int,
    val descripcionCorta: String,
    val pasos: String,
    val ttsText: String,
    val iconAsset: String? = null,
    val orden: Int = 0,
    val activo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)