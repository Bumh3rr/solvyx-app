package com.solvyx.backend.models

/**
 * Prompt de journaling: pregunta/disparador curado por el equipo clínico.
 *
 * Decisiones de modelado:
 * - [slug] se deriva en el mapper a partir de `categoria + orden`
 *   (formato `${categoria}-${orden.toString().padStart(3, '0')}`,
 *   p. ej. `gratitud-001`). Esto evita añadir una columna extra a la
 *   entity y mantiene la PK estable como `id`.
 * - [categoria] es el grupo semántico: `gratitud`, `dificultad`,
 *   `curiosidad`, `emociones`, `cravings`, `planes`.
 */
data class PromptJournaling(
    val id: Int,
    val slug: String,
    val categoria: String,
    val texto: String,
    val orden: Int,
    val activo: Boolean
)
