package com.solvyx.backend.data.model

/**
 * Modelo de dominio de un logro. Reemplaza a la entidad Room `AchievementEntity` (eliminada en
 * una task posterior de esta misma migración): los logros ahora viven solo en Firestore,
 * `users/{uid}/achievements/{achievementId}`, sparse (solo existe doc para los ya desbloqueados).
 */
data class Achievement(
    val id: String,
    val unlocked: Boolean = false,
    val unlockDate: Long? = null
) {
    companion object {
        /**
         * Fuente de verdad de los logros base: los 5 de racha, únicos con lógica de
         * desbloqueo real (`JourneyViewModel.autoUnlock`). El ID codifica el umbral en días.
         */
        val STREAK_THRESHOLDS: Map<String, Int> = mapOf(
            "racha_3" to 3,
            "racha_7" to 7,
            "racha_10" to 10,
            "racha_15" to 15,
            "racha_30" to 30
        )

        val BASE_IDS: List<String> = STREAK_THRESHOLDS.keys.toList()

        /**
         * Los mismos umbrales como lista ordenada de días. "Próximo logro" en Journey ("Mi camino") y
         * "% hacia N días" en la tarjeta de racha de Home son el mismo concepto, así que ambos
         * leen de aquí: si se agrega o mueve un hito, las dos pantallas y el desbloqueo
         * automático quedan de acuerdo por construcción.
         */
        val MILESTONE_DAYS: List<Int> = STREAK_THRESHOLDS.values.sorted()
    }
}
