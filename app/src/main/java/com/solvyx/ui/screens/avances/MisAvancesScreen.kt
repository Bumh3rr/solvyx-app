package com.solvyx.ui.screens.avances

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.R
import com.solvyx.ui.screens.guias.components.BorderCard
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealMedium
import com.solvyx.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisAvancesScreen(
    onOpenDrawer: () -> Unit,
    viewModel: AvancesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showShareSheet by remember { mutableStateOf(false) }

    if (showShareSheet) {
        ShareProgressSheet(
            racha          = viewModel.racha,
            mejorRacha     = viewModel.mejorRacha,
            logrosCount    = viewModel.uiLogros.count { it.unlocked },
            onShare        = { text ->
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type    = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(intent, "Compartir progreso"))
            },
            onDismiss      = { showShareSheet = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 8.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    painter           = painterResource(R.drawable.ic_menu),
                    contentDescription = "Menú",
                    tint              = Color.White,
                    modifier          = Modifier.size(24.dp)
                )
            }
            Text(
                text      = "Mis Avances",
                modifier  = Modifier.weight(1f),
                style     = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color     = Color.White,
                textAlign = TextAlign.Center
            )
            IconButton(onClick = { showShareSheet = true }) {
                Icon(
                    painter           = painterResource(R.drawable.ic_share),
                    contentDescription = "Compartir",
                    tint              = Color.White,
                    modifier          = Modifier.size(24.dp)
                )
            }
        }

        // ── Hero section ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(start = 20.dp, end = 16.dp, top = 8.dp, bottom = 44.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 120.dp)
            ) {
                Text(
                    text  = viewModel.racha.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color.White
                )
                Text(
                    text  = "días sin consumo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text  = "Mejor racha: ${viewModel.mejorRacha} días",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            }
            Image(
                painter           = painterResource(R.drawable.berto_mira_izquierda),
                contentDescription = null,
                modifier          = Modifier
                    .size(110.dp)
                    .align(Alignment.BottomEnd)
                    .offset(y = 16.dp),
                contentScale = ContentScale.Fit
            )
        }

        // ── White panel ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .offset(y = (-24).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 56.dp)
        ) {

            // ── Milestone card (overlapping hero) ─────────────────────────
            Spacer(Modifier.height(8.dp))
            BorderCard(leftBorderColor = TealMedium) {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment   = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment    = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter           = painterResource(R.drawable.ic_trophy),
                                contentDescription = null,
                                tint              = MaterialTheme.colorScheme.primary,
                                modifier          = Modifier.size(16.dp)
                            )
                            Text(
                                text  = "Próximo logro: ${viewModel.proximoLogro} días",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TealDark
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress      = { viewModel.milestoneProgress },
                            modifier      = Modifier.fillMaxWidth(),
                            trackColor    = MaterialTheme.colorScheme.primaryContainer,
                            color         = MaterialTheme.colorScheme.primary,
                            strokeCap     = StrokeCap.Round
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = "${(viewModel.milestoneProgress * 100).toInt()}% completado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Milestone markers: 7, 15, 30 days
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    viewModel.milestoneDays.forEach { days ->
                        val reached = viewModel.racha >= days
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier         = Modifier
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
                                        painter           = painterResource(R.drawable.ic_check_circle),
                                        contentDescription = null,
                                        tint              = Color.White,
                                        modifier          = Modifier.size(14.dp)
                                    )
                                } else {
                                    Text(
                                        text  = days.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text  = "${days}d",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (reached) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Tab selector ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceDim)
                    .border(
                        width  = 0.5.dp,
                        color  = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape  = RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp)
            ) {
                listOf("Semana", "Mes").forEachIndexed { i, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (i == viewModel.selectedTab) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable { viewModel.selectTab(i) }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (i == viewModel.selectedTab) Color.White
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Feelings chart ───────────────────────────────────────────────
            Text(
                text  = "Mi bienestar",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TealDark
            )
            Spacer(Modifier.height(8.dp))
            val feelingsData = if (viewModel.selectedTab == 0)
                viewModel.feelingsDataSemana else viewModel.feelingsDataMes
            val labels = if (viewModel.selectedTab == 0)
                viewModel.labelsSemana else viewModel.labelsMes
            FeelingsChart(
                data     = feelingsData,
                labels   = labels,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // ── Consumption chart ────────────────────────────────────────────
            Text(
                text  = "Días de consumo",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TealDark
            )
            Spacer(Modifier.height(8.dp))
            val consumoData = if (viewModel.selectedTab == 0)
                viewModel.consumoSemana else viewModel.consumoMes
            ConsumptionChart(
                data     = consumoData,
                labels   = labels,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // ── Berto dice insight card ──────────────────────────────────────
            BorderCard(
                leftBorderColor = TealMedium,
                bg              = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter           = painterResource(R.drawable.berto_dedo_der),
                        contentDescription = null,
                        modifier          = Modifier.size(44.dp),
                        contentScale      = ContentScale.Fit
                    )
                    Column {
                        Text(
                            text  = "Berto dice",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text  = "Los miércoles y jueves consumes más. Planifica actividades alternativas esos días.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TealDark
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Logros (horizontal scroll) ───────────────────────────────────
            Text(
                text  = "Logros",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TealDark
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(viewModel.uiLogros) { logro ->
                    LogroCard(logro = logro)
                }
            }
        }
    }
}

// ── Logro Card ───────────────────────────────────────────────────────────────

@Composable
private fun LogroCard(logro: AvancesViewModel.UiLogro) {
    Card(
        modifier = Modifier.width(100.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (logro.unlocked)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceDim
        ),
        border   = BorderStroke(0.5.dp, TealLight)
    ) {
        Column(
            modifier             = Modifier.padding(12.dp),
            horizontalAlignment  = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier         = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (logro.unlocked) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter           = painterResource(logro.icon),
                        contentDescription = null,
                        tint              = if (logro.unlocked) Color.White
                                           else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier          = Modifier.size(22.dp)
                    )
                }
                if (!logro.unlocked) {
                    Box(
                        modifier         = Modifier
                            .size(16.dp)
                            .offset(x = 2.dp, y = 2.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter           = painterResource(R.drawable.ic_lock),
                            contentDescription = null,
                            tint              = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier          = Modifier.size(10.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text      = logro.titulo,
                style     = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color     = if (logro.unlocked) TealDark
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines  = 2
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text      = logro.descripcion,
                style     = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines  = 2
            )
        }
    }
}

// ── Share Progress Sheet ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareProgressSheet(
    racha       : Int,
    mejorRacha  : Int,
    logrosCount : Int,
    onShare     : (String) -> Unit,
    onDismiss   : () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val shareText  = buildShareText(racha, mejorRacha, logrosCount)

    ModalBottomSheet(
        onDismissRequest    = onDismiss,
        sheetState          = sheetState,
        containerColor      = MaterialTheme.colorScheme.background,
        shape               = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text  = "Compartir mi progreso",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TealDark
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Así verán tu avance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            // ── Preview card ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(TealPrimary, TealDark)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment      = Alignment.CenterVertically,
                        horizontalArrangement  = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter           = painterResource(R.drawable.ic_trophy),
                            contentDescription = null,
                            tint              = Color.White.copy(alpha = 0.8f),
                            modifier          = Modifier.size(18.dp)
                        )
                        Text(
                            text  = "Mi progreso en Solvyx",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ShareStat(label = "Racha actual",  value = "$racha días")
                        ShareStat(label = "Mejor racha",   value = "$mejorRacha días")
                        ShareStat(label = "Logros",        value = "$logrosCount")
                    }
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text  = "¡Cada día cuenta 💪",
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick  = { onShare(shareText) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter           = painterResource(R.drawable.ic_share),
                    contentDescription = null,
                    modifier          = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "Compartir ahora",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ShareStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = Color.White
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.75f)
        )
    }
}

private fun buildShareText(racha: Int, mejorRacha: Int, logros: Int): String = """
    🌱 Mi progreso en Solvyx

    🔥 Racha actual: $racha días sin consumo
    🏆 Mejor racha: $mejorRacha días
    ⭐ Logros desbloqueados: $logros

    ¡Cada día cuenta! 💪
""".trimIndent()
