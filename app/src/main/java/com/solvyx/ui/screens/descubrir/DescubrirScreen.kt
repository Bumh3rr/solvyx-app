package com.solvyx.ui.screens.descubrir

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.R
import com.solvyx.ui.components.common.AccessCategory
import com.solvyx.ui.components.common.SolvyxAccessCard

/**
 * Pantalla "Descubrir Solvyx".
 *
 * Hub central con las 6 features nuevas organizadas en 4 categorías
 * temáticas. Es la pantalla que recibe al usuario que pulsa "Ver todo"
 * en la sección "Descubre Solvyx" de Home o el item "Descubrir Solvyx"
 * del drawer.
 *
 * Las 4 categorías:
 *  - **Regular tu día**: Rutinas, Ejercicios.
 *  - **Entender**: Psicoeducación, Insights de Berto.
 *  - **Expresarte**: Journaling.
 *  - **Momentos difíciles**: Guías extendidas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescubrirScreen(
    onNavigateToEjercicios: () -> Unit = {},
    onNavigateToRutinas: () -> Unit = {},
    onNavigateToPsicoeducacion: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToJournaling: () -> Unit = {},
    onNavigateToGuiasExtendidas: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    "Descubre Solvyx",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_left),
                        contentDescription = "Volver"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // Subtítulo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Column {
                Text(
                    text = "Recursos para acompañarte.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Categoría 1: Regular tu día ───────────────
            item {
                CategoriaSeccion(
                    titulo = "Regular tu día",
                    descripcion = "Rutinas y ejercicios para momentos cotidianos."
                )
            }
            item {
                CategoriaCards(
                    cards = listOf(
                        DescubrirCardConfig(
                            titulo = "Rutinas",
                            subtitulo = "Matutina y nocturna",
                            iconRes = R.drawable.ic_calendar,
                            category = AccessCategory.Calm,
                            isNew = true,
                            onClick = onNavigateToRutinas
                        ),
                        DescubrirCardConfig(
                            titulo = "Ejercicios",
                            subtitulo = "Respiración, body scan, grounding",
                            iconRes = R.drawable.ic_wind,
                            category = AccessCategory.Calm,
                            isNew = true,
                            onClick = onNavigateToEjercicios
                        )
                    )
                )
            }

            // ── Categoría 2: Entender ──────────────────────
            item {
                CategoriaSeccion(
                    titulo = "Entender",
                    descripcion = "Aprende sobre consumo, sustancias y patrones."
                )
            }
            item {
                CategoriaCards(
                    cards = listOf(
                        DescubrirCardConfig(
                            titulo = "Psicoeducación",
                            subtitulo = "24 lecciones por sustancia",
                            iconRes = R.drawable.ic_brain,
                            category = AccessCategory.Learn,
                            isNew = true,
                            onClick = onNavigateToPsicoeducacion
                        ),
                        DescubrirCardConfig(
                            titulo = "Insights de Berto",
                            subtitulo = "Patrones que Berto nota",
                            iconRes = R.drawable.ic_chart_bar,
                            category = AccessCategory.Learn,
                            isNew = true,
                            onClick = onNavigateToInsights
                        )
                    )
                )
            }

            // ── Categoría 3: Expresarte ────────────────────
            item {
                CategoriaSeccion(
                    titulo = "Expresarte",
                    descripcion = "Escribe con prompts o en libertad."
                )
            }
            item {
                CategoriaCards(
                    cards = listOf(
                        DescubrirCardConfig(
                            titulo = "Journaling",
                            subtitulo = "30+ prompts por categoría",
                            iconRes = R.drawable.ic_pencil,
                            category = AccessCategory.Express,
                            isNew = true,
                            onClick = onNavigateToJournaling
                        )
                    )
                )
            }

            // ── Categoría 4: Momentos difíciles ──────────
            item {
                CategoriaSeccion(
                    titulo = "Momentos difíciles",
                    descripcion = "Guías paso a paso para cuando lo necesitas."
                )
            }
            item {
                CategoriaCards(
                    cards = listOf(
                        DescubrirCardConfig(
                            titulo = "Guías extendidas",
                            subtitulo = "8 guías adicionales de apoyo",
                            iconRes = R.drawable.ic_clipboard,
                            category = AccessCategory.Support,
                            isNew = true,
                            onClick = onNavigateToGuiasExtendidas
                        )
                    )
                )
            }

            // ── Footer ─────────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_shield),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "Todo este contenido vive solo en tu teléfono. No se envía a ningún servidor.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private data class DescubrirCardConfig(
    val titulo: String,
    val subtitulo: String,
    val iconRes: Int,
    val category: AccessCategory,
    val isNew: Boolean,
    val onClick: () -> Unit
)

@Composable
private fun CategoriaSeccion(
    titulo: String,
    descripcion: String
) {
    Column {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = descripcion,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategoriaCards(cards: List<DescubrirCardConfig>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        cards.chunked(2).forEach { rowItems ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { card ->
                    SolvyxAccessCard(
                        title = card.titulo,
                        description = card.subtitulo,
                        iconRes = card.iconRes,
                        category = card.category,
                        isNew = card.isNew,
                        onClick = card.onClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
