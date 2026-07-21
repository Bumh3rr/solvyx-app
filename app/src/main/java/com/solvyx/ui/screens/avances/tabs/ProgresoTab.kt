package com.solvyx.ui.screens.avances.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxSegmentedControl
import com.solvyx.ui.components.navigation.SolvyxBottomNavHeight
import com.solvyx.ui.screens.avances.AvancesViewModel
import com.solvyx.ui.screens.avances.ConsumptionChart
import com.solvyx.ui.screens.avances.FeelingsChart
import com.solvyx.ui.screens.guias.components.BorderCard
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealMedium

@Composable
fun ProgresoTab(
    viewModel: AvancesViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = SolvyxBottomNavHeight)
    ) {
        // ── Hero de racha ─────────────────────────────────────────────
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text(
                    text = viewModel.racha.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "días sin consumo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Mejor racha: ${viewModel.mejorRacha} días",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Image(
                painter = painterResource(R.drawable.berto_mira_izquierda),
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.CenterEnd),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Milestone card ────────────────────────────────────────────
        BorderCard(leftBorderColor = TealMedium) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_trophy),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Próximo logro: ${viewModel.proximoLogro} días",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TealDark
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { viewModel.milestoneProgress },
                        modifier = Modifier.fillMaxWidth(),
                        trackColor = MaterialTheme.colorScheme.primaryContainer,
                        color = MaterialTheme.colorScheme.primary,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${(viewModel.milestoneProgress * 100).toInt()}% completado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                viewModel.milestoneDays.forEach { days ->
                    val reached = viewModel.racha >= days
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (reached) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primaryContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (reached) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check_circle),
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text(
                                    text = days.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${days}d",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (reached) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Selector Semana / Mes ─────────────────────────────────────
        SolvyxSegmentedControl(
            options = listOf("Semana", "Mes"),
            selectedIndex = viewModel.selectedTab,
            onSelect = { viewModel.selectTab(it) }
        )

        Spacer(Modifier.height(20.dp))

        val labels = if (viewModel.selectedTab == 0) viewModel.labelsSemana else viewModel.labelsMes

        // ── Gráfica de bienestar ──────────────────────────────────────
        Text(
            text = "Mi bienestar",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TealDark
        )
        Spacer(Modifier.height(8.dp))
        FeelingsChart(
            data = if (viewModel.selectedTab == 0) viewModel.feelingsDataSemana else viewModel.feelingsDataMes,
            labels = labels,
            modifier = Modifier.fillMaxWidth(),
            onPointSelected = { viewModel.onChartPointSelected(it) }
        )

        Spacer(Modifier.height(20.dp))

        // ── Gráfica de consumo ────────────────────────────────────────
        Text(
            text = "Días de consumo",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TealDark
        )
        Spacer(Modifier.height(8.dp))
        ConsumptionChart(
            data = if (viewModel.selectedTab == 0) viewModel.consumoSemana else viewModel.consumoMes,
            labels = labels,
            modifier = Modifier.fillMaxWidth(),
            onPointSelected = { viewModel.onChartPointSelected(it) }
        )

        Spacer(Modifier.height(20.dp))

        // ── "Berto dice" ──────────────────────────────────────────────
        BorderCard(
            leftBorderColor = TealMedium,
            bg = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.berto_dedo_der),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    contentScale = ContentScale.Fit
                )
                Column {
                    Text(
                        text = "Berto dice",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = viewModel.bertoInsight,
                        style = MaterialTheme.typography.bodySmall,
                        color = TealDark
                    )
                }
            }
        }
    }
}
