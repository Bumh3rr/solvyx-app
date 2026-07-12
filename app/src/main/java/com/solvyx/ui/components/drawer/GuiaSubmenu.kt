package com.solvyx.ui.components.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealMedium

/**
 * Submenú de Guías de primeros auxilios.
 *
 * Renderiza un item padre "Guías de primeros auxilios" con un chevron
 * que se expande/colapsa para mostrar dos subsecciones:
 *   1. Guías originales (5)
 *   2. Guías extendidas (8) - marcadas con badge NUEVO
 *
 * @param expandedByDefault si arranca expandido o colapsado
 * @param onOriginalClick callback al tap de una guía original
 * @param onExtendidaClick callback al tap de una guía extendida
 * @param selectedSlug slug de la guía actualmente seleccionada (resaltado)
 */
@Composable
fun GuiaSubmenu(
    expandedByDefault: Boolean = false,
    onToggle: () -> Unit = {},
    onOriginalClick: (slug: String) -> Unit = {},
    onExtendidaClick: (slug: String) -> Unit = {},
    selectedSlug: String? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(expandedByDefault) }
    val originalCount = GUIAS_ORIGINALES.size
    val extendidaCount = GUIAS_EXTENDIDAS.size

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (selectedSlug != null) 1.dp else 0.dp,
                color = if (selectedSlug != null) Color.White.copy(alpha = 0.18f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        // Header del submenú
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (selectedSlug != null) Color.White.copy(alpha = 0.14f)
                    else Color.Transparent
                )
                .clickable {
                    expanded = !expanded
                    onToggle()
                }
                .padding(horizontal = 14.dp, vertical = 15.dp)
                .semantics {
                    contentDescription = if (expanded) {
                        "Colapsar submenú de guías"
                    } else {
                        "Expandir submenú de guías"
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(
                    com.solvyx.R.drawable.ic_guide
                ),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.95f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = "Guías de primeros auxilios",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            NewBadge(label = "${originalCount + extendidaCount}")
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowDown
                              else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TealLight,
                modifier = Modifier.size(18.dp)
            )
        }

        // Contenido expandido
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, top = 4.dp, bottom = 8.dp, end = 8.dp)
            ) {
                // Sección: 5 originales
                SubmenuSectionHeader("Crisis y cravings (5)")
                GUIAS_ORIGINALES.forEach { (slug, titulo) ->
                    GuiaSubmenuItem(
                        titulo = titulo,
                        slug = slug,
                        isNew = false,
                        isSelected = selectedSlug == slug,
                        onClick = { onOriginalClick(slug) }
                    )
                }
                Spacer(Modifier.height(8.dp))

                // Sección: 8 extendidas
                SubmenuSectionHeader(
                    "Extendidas (8)",
                    highlight = true
                )
                GUIAS_EXTENDIDAS.forEach { (slug, titulo) ->
                    GuiaSubmenuItem(
                        titulo = titulo,
                        slug = slug,
                        isNew = true,
                        isSelected = selectedSlug == slug,
                        onClick = { onExtendidaClick(slug) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubmenuSectionHeader(
    titulo: String,
    highlight: Boolean = false
) {
    Text(
        text = titulo,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = if (highlight) TealLight else Color.White.copy(alpha = 0.5f),
        letterSpacing = 1.0.sp,
        modifier = Modifier.padding(start = 28.dp, top = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun GuiaSubmenuItem(
    titulo: String,
    slug: String,
    isNew: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) Color.White.copy(alpha = 0.10f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .semantics {
                contentDescription = if (isNew) {
                    "Guía nueva: $titulo"
                } else {
                    titulo
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bullet point
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(if (isSelected) Color.White else TealMedium)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = titulo,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
            modifier = Modifier.weight(1f)
        )
        if (isNew) {
            NewBadge(label = "NUEVO")
        }
    }
}

/** 5 guías originales (de GuiasNavGraph) */
private val GUIAS_ORIGINALES: List<Pair<String, String>> = listOf(
    "guia_crisis_id" to "Cómo sé si estoy en crisis",
    "guia_panico" to "Ansiedad y ataque de pánico",
    "guia_craving_intenso" to "Craving muy intenso",
    "guia_consumi_de_mas" to "Consumí de más",
    "guia_estoy_en_crisis" to "Estoy en crisis ahora mismo"
)

/** 8 guías extendidas (de GuiasExtendidasScreen) */
private val GUIAS_EXTENDIDAS: List<Pair<String, String>> = listOf(
    "desregulacion-flashback" to "Desregulación / flashback",
    "intoxicacion-alcohol-esperando" to "Intoxicación: esperando que pase",
    "craving-extremo" to "Craving extremo (después de consumir)",
    "noche-dificil" to "Noche difícil (insomnio, madrugada)",
    "conflicto-familia" to "Conflicto con familia",
    "violencia-sexual-reciente" to "Violencia sexual reciente",
    "volver-de-fiesta" to "Volver de fiesta",
    "despues-de-consumir-de-nuevo" to "Después de consumir de nuevo"
)
