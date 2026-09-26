package com.solvyx.ui.screens.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.theme.TealLight

private const val QuickAccessColumns = 3
private const val IconCapsuleAlpha = 0.45f

private data class QuickAccessItem(
    val title: String,
    val subtitle: String,
    @DrawableRes val icon: Int,
    val onClick: () -> Unit
)

/**
 * "Accesos rápidos" de Home: rejilla de 3 columnas con los seis atajos. Los títulos e íconos
 * coinciden con el drawer y el bottom nav (p. ej. "Mi camino" con `ic_footsteps`), para que un
 * mismo destino se reconozca igual en toda la app.
 */
@Composable
fun HomeQuickAccess(
    onNavigateToPlan: () -> Unit,
    onNavigateToJourney: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToBreathing: () -> Unit,
    onNavigateToFirstAid: () -> Unit,
    onNavigateToSupportNetwork: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        QuickAccessItem("Mi Plan", "Tu meta de hoy", R.drawable.ic_plan, onNavigateToPlan),
        QuickAccessItem("Mi camino", "Registra tu día", R.drawable.ic_footsteps, onNavigateToJourney),
        QuickAccessItem("Hablar con Berto", "Disponible ahora", R.drawable.ic_chat, onNavigateToChat),
        QuickAccessItem("Respirar", "Ejercicio 5-4-3-2-1", R.drawable.ic_wind, onNavigateToBreathing),
        QuickAccessItem("Primeros auxilios", "Funciona sin internet", R.drawable.ic_guide, onNavigateToFirstAid),
        QuickAccessItem("Mi red de apoyo", "Contactos de confianza", R.drawable.ic_people, onNavigateToSupportNetwork)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Accesos rápidos",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.chunked(QuickAccessColumns).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        QuickAccessCard(
                            item = item,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                    // Rellena la última fila incompleta para que las tarjetas no se estiren.
                    repeat(QuickAccessColumns - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** Un atajo: ícono en su cápsula + título + subtítulo, con el look de [SolvyxCard]. */
@Composable
private fun QuickAccessCard(item: QuickAccessItem, modifier: Modifier = Modifier) {
    SolvyxCard(modifier = modifier, onClick = item.onClick) {
        Column(Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(TealLight.copy(alpha = IconCapsuleAlpha), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(item.icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                item.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
