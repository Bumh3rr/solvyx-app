package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entrada escrita por el usuario en respuesta a un prompt de journaling.
 *
 * Dominio: journaling libre. Una entrada referencia opcionalmente un
 * [PromptJournalingEntity] (FK lógica, sin `ForeignKey` para permitir que se
 * borren prompts sin perder el historial).
 *
 * Por qué no hay `ForeignKey` real:
 * - El snapshot ([promptTexto]) guarda el texto del prompt en el momento de
 *   la escritura, así que la entrada sigue siendo legible si el prompt
 *   desaparece.
 * - Si quisiéramos integridad referencial estricta, lo natural sería SET NULL
 *   en lugar de CASCADE; pero al ser solo referencia lógica y no usarse para
 *   joins en caliente, se evita la sobrecarga.
 *
 * [contenido] se almacena tal cual (texto largo, sin cifrar). Si más adelante
 * se requiere cifrado en reposo, se hará en una migración posterior o en la
 * capa de repositorio antes de persistir.
 */
@Entity(
    tableName = "journaling_entries",
    indices = [
        Index(value = ["fecha"], name = "idx_journaling_fecha"),
        Index(value = ["promptId"], name = "idx_journaling_promptId")
    ]
)
data class JournalingEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: Long,
    val promptId: Int? = null,
    val promptTexto: String? = null,
    val contenido: String,
    val createdAt: Long = System.currentTimeMillis()
)