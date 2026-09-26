package com.solvyx.ui.components.common

import androidx.compose.ui.graphics.Color
import com.solvyx.backend.models.NivelRiesgo
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.CrisisRedLight
import com.solvyx.ui.theme.RiskLow
import com.solvyx.ui.theme.RiskLowContainer
import com.solvyx.ui.theme.RiskModerate
import com.solvyx.ui.theme.WarnAmber

/**
 * ASSIST risk band. [storedValue] is what `LastAssistEntity.level` holds; [zoneCenter] is where
 * the gauge marker rests (0..1) — the center of the band, since the exact score thresholds differ
 * per substance and the gauge only claims the band, not a precise position.
 */
enum class AssistRiskLevel(
    val storedValue: String,
    val label: String,
    val accent: Color,
    val container: Color,
    val zoneCenter: Float
) {
    LOW("BAJO", "Bajo", RiskLow, RiskLowContainer, 1f / 6f),
    MODERATE("MODERADO", "Moderado", RiskModerate, WarnAmber, 3f / 6f),
    HIGH("ALTO", "Alto", CrisisRed, CrisisRedLight, 5f / 6f);

    companion object {
        fun fromStored(value: String): AssistRiskLevel? = entries.firstOrNull { it.storedValue == value }
        fun from(level: NivelRiesgo): AssistRiskLevel = fromStored(level.name) ?: LOW
    }
}
