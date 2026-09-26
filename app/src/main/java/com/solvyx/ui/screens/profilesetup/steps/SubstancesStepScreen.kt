package com.solvyx.ui.screens.profilesetup.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.berto.BertoSpeechRow
import com.solvyx.ui.components.common.SubstanceCard
import com.solvyx.ui.components.common.TrackedSubstances
import com.solvyx.ui.screens.profilesetup.ProfileSetupStep
import com.solvyx.ui.screens.profilesetup.components.SetupScaffold
import com.solvyx.ui.theme.TealDark

private const val GridColumns = 2

/** Step 1: which substances to track. Several can be chosen; it can be changed later in Mi perfil. */
@Composable
fun SubstancesStepScreen(
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: (() -> Unit)?
) {
    SetupScaffold(
        step = ProfileSetupStep.SUBSTANCES,
        onBack = onBack,
        primaryLabel = if (selected.isEmpty()) "Elige al menos una" else "Continuar",
        primaryEnabled = selected.isNotEmpty(),
        onPrimary = onContinue
    ) {
        BertoSpeechRow(
            message = "¿Qué quieres cuidar?",
            supporting = "Puedes elegir varias. Nadie más lo ve y lo puedes cambiar después en Mi perfil."
        ) {
            BertoPoseAnimation(
                pose = BertoPose.CENTER_IDLE_TO_RIGHT,
                riveFileRes = R.raw.berto_poses,
                modifier = Modifier.fillMaxSize(),
                fallback = R.drawable.berto_dedo_der
            )
        }
        Text(
            text = "Sustancias que quieres seguir",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = TealDark
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TrackedSubstances.chunked(GridColumns).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { (id, label, icon) ->
                        SubstanceCard(
                            id = id,
                            label = label,
                            iconRes = icon,
                            selected = id in selected,
                            onClick = { onToggle(id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
