package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Guía extendida para contextos específicos de apoyo (crisis, craving, post-consumo, etc.).
 *
 * Dominio: contenido educativo + procedimental para acompañar al usuario en
 * situaciones difíciles detectadas por el árbol de decisión o bitácora.
 *
 * Campo JSON (parseado en repositorio con Gson):
 * - [contenido]: objeto JSON con la forma:
 *   ```
 *   {
 *     "introduccion": "string",
 *     "pasos": [{"orden":1, "titulo":"...", "descripcion":"..."}],
 *     "señalesAlerta": ["string", ...],
 *     "cuandoLlamar911": ["string", ...],
 *     "lineasAyuda": [{"nombre":"...", "telefono":"..."}]
 *   }
 *   ```
 *
 * El JSON completo es deliberadamente opaco a Room para poder evolucionar el
 * esquema de la guía sin tocar migraciones de SQLite. Solo cambia [titulo],
 * [categoria] y metadatos a nivel de tabla.
 */
@Entity(
    tableName = "guias_extendidas",
    indices = [
        Index(value = ["slug"], unique = true),
        Index(value = ["categoria"], name = "idx_guias_categoria")
    ]
)
data class GuiaExtendidaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val slug: String,
    val titulo: String,
    val categoria: String,
    val descripcionCorta: String,
    val contenido: String,
    val iconAsset: String? = null,
    val orden: Int = 0,
    val activo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)