package com.solvyx.ui.screens.plan

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.components.dialog.SosConfirmationDialog
import com.solvyx.ui.screens.guias.components.BorderCard
import com.solvyx.ui.screens.guias.components.DotRow
import com.solvyx.ui.screens.guias.components.GuiaPanel
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.screens.guias.components.HeroSideBerto
import com.solvyx.ui.screens.guias.components.StepRow
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

@Composable
fun ManejoCravingScreen(
    onBack: () -> Unit,
    onNavigateToEjercicio: () -> Unit,
    onNavigateToRedApoyo: () -> Unit,
    viewModel: PlanViewModel = hiltViewModel()
) {
    var expandedCard by remember { mutableStateOf(-1) }

    if (viewModel.showSosDialog) {
        SosConfirmationDialog(
            onConfirm = {
                viewModel.cerrarSosDialog()
                onNavigateToRedApoyo()
            },
            onDismiss = { viewModel.cerrarSosDialog() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        GuiaTopBar(
            title = "Manejo del craving",
            onBack = onBack
        )

        HeroSideBerto(
            mascot = R.drawable.berto_preocupado,
            title = "El craving es temporal",
            subtitle = "Con las herramientas correctas puedes manejarlo"
        )

        GuiaPanel(modifier = Modifier.weight(1f)) {

            // ── Info pill ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "El craving dura entre 15 y 20 minutos.",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Accordion 0: Respiración cuadrada ─────────────────────────
            AccordionCard(
                index = 0,
                expandedCard = expandedCard,
                onToggle = { expandedCard = if (expandedCard == 0) -1 else 0 },
                title = "Respiración cuadrada",
                icon = R.drawable.ic_heart_pulse
            ) {
                Text(
                    text = "Inhala, aguanta, exhala y pausa durante 4 segundos cada fase.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                StepRow(1, "Inhala lentamente por 4 segundos")
                StepRow(2, "Aguanta el aire 4 segundos")
                StepRow(3, "Exhala lentamente por 4 segundos")
                StepRow(4, "Espera 4 segundos antes de repetir")
                Spacer(Modifier.height(12.dp))
                SolvyxButton(
                    text = "Iniciar ejercicio guiado",
                    onClick = onNavigateToEjercicio,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Accordion 1: Técnica 5-4-3-2-1 ──────────────────────────
            AccordionCard(
                index = 1,
                expandedCard = expandedCard,
                onToggle = { expandedCard = if (expandedCard == 1) -1 else 1 },
                title = "Técnica 5-4-3-2-1",
                icon = R.drawable.ic_brain
            ) {
                Text(
                    text = "Nombra 5 cosas que ves, 4 que tocas, 3 que escuchas, 2 que hueles y 1 que saboreas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                SolvyxButton(
                    text = "Iniciar ejercicio guiado",
                    onClick = onNavigateToEjercicio,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Accordion 2: Distracción activa ──────────────────────────
            AccordionCard(
                index = 2,
                expandedCard = expandedCard,
                onToggle = { expandedCard = if (expandedCard == 2) -1 else 2 },
                title = "Distracción activa",
                icon = R.drawable.ic_flame
            ) {
                DotRow(text = "Sal a caminar por 10 minutos")
                DotRow(text = "Llama o escribe a alguien")
                DotRow(text = "Pon música que te guste")
                DotRow(text = "Come algo ligero o toma agua")
            }

            Spacer(Modifier.height(8.dp))

            // ── Accordion 3: Llamar a alguien ─────────────────────────────
            AccordionCard(
                index = 3,
                expandedCard = expandedCard,
                onToggle = { expandedCard = if (expandedCard == 3) -1 else 3 },
                title = "Llamar a alguien",
                icon = R.drawable.ic_people
            ) {
                Text(
                    text = "Contacta a alguien de confianza. Tu red de apoyo está ahí para momentos como este.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                SolvyxButton(
                    text = "Ver mi red de apoyo",
                    onClick = onNavigateToRedApoyo,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── SOS ghost button ──────────────────────────────────────────
            SolvyxOutlinedButton(
                text = "Avisar a mi red de apoyo",
                onClick = { viewModel.abrirSosDialog() },
                modifier = Modifier.fillMaxWidth(),
                borderColor = CrisisRed,
                textColor = CrisisRed
            )
        }
    }
}

@Composable
private fun AccordionCard(
    index: Int,
    expandedCard: Int,
    onToggle: () -> Unit,
    title: String,
    @DrawableRes icon: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    BorderCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TealDark,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(
                    if (expandedCard == index) R.drawable.ic_chevron_down
                    else R.drawable.ic_chevron_right
                ),
                contentDescription = null,
                tint = TealLight,
                modifier = Modifier.size(16.dp)
            )
        }
        AnimatedVisibility(
            visible = expandedCard == index,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                HorizontalDivider(
                    color = TealLight,
                    thickness = 0.5.dp
                )
                Spacer(Modifier.height(8.dp))
                content()
            }
        }
    }
}
