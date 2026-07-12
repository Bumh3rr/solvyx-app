package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entrada de bitácora diaria del usuario.
 *
 * Dominio: un registro por sesión de check-in. Mantiene los campos base del
 * MVP (estado de ánimo, consumo, sustancia, nota) y los campos extendidos
 * introducidos en la migración v2→v3 para alimentar el módulo de insights
 * y el árbol de decisión con más contexto.
 *
 ** Campos base (v1+v2): ** siempre presentes.
 * - [estadoAnimo]: etiqueta libre (`"feliz"`, `"ansioso"`, etc.).
 * - [consumio]: bandera booleana de consumo en el día.
 * - [sustancia]: opcional, FK lógica al catálogo de sustancias.
 * - [nota]: nota libre del usuario.
 *
 ** Campos extendidos (v3, todos nullable): ** son opcionales para no romper
 * los registros existentes. Los nuevos registros pueden llenarlos todos o
 * ninguno; el usuario decide qué compartir.
 *
 * - [suenoHoras] / [suenoCalidad] (1-5): calidad y horas de sueño.
 * - [comio] / [calidadComida] (1-5): ingesta y calidad percibida.
 * - [actividadFisica]: `"ninguna" | "poca" | "moderada" | "intensa"`.
 * - [contextoSocial]: `"solo" | "familia" | "amigos" | "fiesta" | "trabajo"`.
 * - [detonantePrincipal]: etiqueta libre (`"estres"`, `"aburrimiento"`, ...).
 * - [nivelAnsiedad] (0-10): escala analógica auto-reportada.
 * - [tuvoCraving]: bandera para alimentar gráficas de craving.
 * - [ejercicioFisico]: si realizó ejercicio físico (separado de la intensidad).
 * - [notaPrivada]: nota que el usuario marca como privada. En el futuro puede
 *   cifrarse a nivel repositorio; hoy se almacena en claro porque Room no
 *   soporta cifrado por columna. La capa de UI será la responsable de ocultar
 *   este campo en vistas compartidas.
 * - [updatedAt]: timestamp de la última edición. Se usa para sync
 *   incremental y resolución de conflictos en versiones futuras con backend.
 *
 * Nota: la tabla se llama `bitacora` (sin sufijo _extendida) porque
 * sigue siendo la misma entidad conceptual; lo que cambió es su richness,
 * no su identidad.
 */
@Entity(tableName = "bitacora")
data class BitacoraEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: Long,
    val estadoAnimo: String,
    val consumio: Boolean,
    val sustancia: String? = null,
    val nota: String? = null,

    // --- Campos extendidos (v3) ---
    val suenoHoras: Int? = null,
    val suenoCalidad: Int? = null,
    val comio: Boolean? = null,
    val calidadComida: Int? = null,
    val actividadFisica: String? = null,
    val contextoSocial: String? = null,
    val detonantePrincipal: String? = null,
    val nivelAnsiedad: Int? = null,
    val tuvoCraving: Boolean? = null,
    val ejercicioFisico: Boolean? = null,
    val notaPrivada: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)