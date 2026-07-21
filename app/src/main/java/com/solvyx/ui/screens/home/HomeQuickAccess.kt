package com.solvyx.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxCard

private const val QuickAccessColumns = 3

class AccesoItem(val titulo: String, val subtitulo: String, val iconRes: Int, val usePrimary: Boolean = true, val onClick: () -> Unit = {})

/**
 * Sección "Accesos rápidos" de Home: el título y la rejilla de 3 columnas con los seis atajos.
 *
 * Arma la lista de destinos internamente a partir de los callbacks de navegación, así el orden y
 * el copy viven en un solo lugar en vez de esparcirse por la pantalla.
 */
@Composable
fun HomeQuickAccess(
    onNavigateToPlan: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToGuias: () -> Unit,
    onNavigateToRegistro: () -> Unit,
    onNavigateToRedApoyo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accesos = listOf(
        AccesoItem("Mi Plan",           "Meta de hoy lista",             R.drawable.ic_target,      usePrimary = true,  onClick = onNavigateToPlan),
        AccesoItem("Técnicas",          "Manejo y reducción",            R.drawable.ic_brain,       usePrimary = false, onClick = onNavigateToPlan),
        AccesoItem("Hablar con Berto",  "Disponible ahora",              R.drawable.ic_chat,        usePrimary = true,  onClick = onNavigateToChat),
        AccesoItem("Primeros Auxilios", "Sin conexión a internet",       R.drawable.ic_guide,       usePrimary = false, onClick = onNavigateToGuias),
        AccesoItem("Mi Registro",       "Registrar hoy o ver historial", R.drawable.ic_trending_up, usePrimary = true,  onClick = onNavigateToRegistro),
        AccesoItem("Mi Red de Apoyo",   "Contactos de confianza",        R.drawable.ic_people,      usePrimary = false, onClick = onNavigateToRedApoyo)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Accesos rápidos",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            accesos.chunked(QuickAccessColumns).forEach { rowItems ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        AccesoRapidoCard(
                            item = item,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Rellena la última fila incompleta para que las tarjetas no se estiren.
                    repeat(QuickAccessColumns - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/**
 * Tarjeta de un acceso rápido: icono en su cápsula de color + título + subtítulo. Usa [SolvyxCard]
 * para heredar el look de tarjeta de la app.
 */
@Composable
private fun AccesoRapidoCard(
    item: AccesoItem,
    modifier: Modifier = Modifier
) {
    val iconContainerColor = if (item.usePrimary) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.secondaryContainer

    SolvyxCard(modifier = modifier, onClick = item.onClick) {
        Column(Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(item.iconRes),
                    contentDescription = item.titulo,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                item.titulo,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                item.subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
