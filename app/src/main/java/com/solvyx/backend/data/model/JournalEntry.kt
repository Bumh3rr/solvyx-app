package com.solvyx.backend.data.model

import java.time.LocalDate

/**
 * Modelo de dominio de una entrada de bitácora. Reemplaza a la entidad Room `JournalEntity`
 * (eliminada en la Task 5): la bitácora ahora vive solo en Firestore, un doc por día cuyo ID es
 * la fecha `yyyy-MM-dd`.
 *
 * `mood`/`consumed` son nullable a propósito: un doc puede existir con solo `metaLograda` (escrito
 * por "Lo logré hoy" de Mi Plan) antes de un registro completo. Un día "registrado" tiene mood.
 * `consumed = null` significa "no tocar este campo en el merge", no "no consumió".
 */
data class JournalEntry(
    val date: LocalDate,
    val mood: String? = null,
    val note: String? = null,
    val consumed: Boolean? = null,
    val substance: String? = null,
    val cantidadAprox: String? = null,
    val notaContexto: String? = null,
    val metaLograda: Boolean = false
) {
    val isRegistered: Boolean get() = mood != null
}
