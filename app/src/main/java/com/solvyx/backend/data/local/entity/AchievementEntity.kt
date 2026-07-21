package com.solvyx.backend.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val unlocked: Boolean = false,
    val unlockDate: Long? = null
) {
    companion object {
        /**
         * Fuente de verdad de los logros base: los 5 de racha, únicos con lógica de
         * desbloqueo real (`AvancesViewModel.autoUnlock`). El ID codifica el umbral en días.
         *
         * La tabla debe contener siempre estas filas (bloqueadas si no se han conseguido).
         * La siembran `AppDatabase.SEED_CALLBACK` al crear la DB y
         * `ProgressRepository.ensureAchievementsSeeded()` después de cada `clearAllTables()`.
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
         * Los mismos umbrales como lista ordenada de días. "Próximo logro" en Mis Avances y
         * "% hacia N días" en la tarjeta de racha de Home son el mismo concepto, así que ambos
         * leen de aquí: si se agrega o mueve un hito, las dos pantallas y el desbloqueo
         * automático quedan de acuerdo por construcción.
         */
        val MILESTONE_DAYS: List<Int> = STREAK_THRESHOLDS.values.sorted()
    }
}
