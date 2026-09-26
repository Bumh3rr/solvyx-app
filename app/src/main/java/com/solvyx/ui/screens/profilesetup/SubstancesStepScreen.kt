package com.solvyx.ui.screens.profilesetup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SubstanceCard
import com.solvyx.ui.components.common.TrackedSubstances
import com.solvyx.ui.components.common.WizardProgressDots

@Composable
fun SubstancesStepScreen(
    viewModel: ProfileSetupViewModel,
    onContinue: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        WizardProgressDots(currentStep = 0, totalSteps = 3, onBack = null)

        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "¿Qué sustancias quieres dar seguimiento?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Puedes cambiar esto después en tu perfil",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 20.dp)
            )

            TrackedSubstances.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { (id, label, icon) ->
                        SubstanceCard(
                            id = id,
                            label = label,
                            iconRes = icon,
                            selected = viewModel.selectedSubstances.contains(id),
                            onClick = { viewModel.toggleSubstance(id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp)) {
            SolvyxButton(
                text = "Continuar →",
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.selectedSubstances.isNotEmpty()
            )
        }
    }
}
