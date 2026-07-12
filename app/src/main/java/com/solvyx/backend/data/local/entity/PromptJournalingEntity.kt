package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Prompt de journaling que se ofrece al usuario como pregunta/disparador.
 *
 * Dominio: banco de preguntas agrupadas por [categoria]
 * (`gratitud`, `dificultad`, `curiosidad`, `emociones`, `cravings`, `planes`).
 * El motor de journaling escoge un prompt por sesión y lo snapshot-ea en
 * [JournalingEntryEntity.promptTexto] para que borrar un prompt aquí no rompa
 * las entradas históricas.
 *
 * El contenido es texto plano corto (≤ 200 caracteres). No requiere JSON.
 */
@Entity(
    tableName = "prompts_journaling",
    indices = [
        Index(value = ["categoria"], name = "idx_prompts_categoria"),
        Index(value = ["orden"], name = "idx_prompts_orden")
    ]
)
data class PromptJournalingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoria: String,
    val texto: String,
    val orden: Int = 0,
    val activo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)