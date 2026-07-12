package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Lección educativa por sustancia (alcohol, vape, cristal, tabaco) y tema.
 *
 * Dominio: contenido de aprendizaje profundo dentro del módulo "Educación".
 * Cada lección pertenece a un par `(sustancia, tema)` y tiene un [orden] propio
 * dentro de ese par para soportar itinerarios (ej. tema "engancha" → 3 lecciones
 * en secuencia).
 *
 * Campo JSON (parseado en repositorio con Gson):
 * - [contenido]: objeto JSON con la forma:
 *   ```
 *   {
 *     "introduccion": "string",
 *     "secciones": [{"orden":1, "titulo":"...", "cuerpo":"...", "imagenAsset":null}],
 *     "conclusion": "string"
 *   }
 *   ```
 *
 * Índices:
 * - `idx_lecciones_sustancia` acelera filtros por sustancia (ej. pantalla "Lecciones de alcohol").
 * - `idx_lecciones_sustancia_tema` acelera filtros compuestos en la ruta de aprendizaje.
 */
@Entity(
    tableName = "lecciones",
    indices = [
        Index(value = ["slug"], unique = true),
        Index(value = ["sustancia"], name = "idx_lecciones_sustancia"),
        Index(value = ["sustancia", "tema"], name = "idx_lecciones_sustancia_tema")
    ]
)
data class LeccionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val slug: String,
    val sustancia: String,
    val tema: String,
    val titulo: String,
    val contenido: String,
    val duracionLecturaMinutos: Int = 0,
    val orden: Int = 0,
    val activo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)