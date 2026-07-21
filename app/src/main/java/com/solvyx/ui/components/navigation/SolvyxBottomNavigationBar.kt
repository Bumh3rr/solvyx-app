package com.solvyx.ui.components.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.R
import com.solvyx.ui.components.haze.LocalHazeState
import dev.chrisbanes.haze.hazeChild

enum class SolvyxBottomTab { INICIO, PLAN, CHATBOT, AVANCES }

/** Alto total de la barra + margen de respiro. Pantallas cuyo contenido pasa detrás del
 *  bottom nav (vía [dev.chrisbanes.haze.haze]) deben dejar al menos esto de padding inferior. */
val SolvyxBottomNavHeight: Dp = 88.dp

@Composable
fun SolvyxBottomNavigationBar(
    selectedTab: SolvyxBottomTab,
    onTabSelected: (SolvyxBottomTab) -> Unit,
    onSosClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Layout: [Inicio][Plan] [Berto-center] [Avances]
    // Berto slot is at position 3/4 (62.5% from left).
    // bertoXOffset shifts the TopCenter overlay (50%) right by 1/8 of width → 62.5%.
    val hazeState = LocalHazeState.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val bertoXOffset = maxWidth * 0.125f

        // ── Bar background with 4 slots (glass/blur vía Haze) ─
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.BottomCenter)
                .hazeChild(
                    state = hazeState,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavTab(
                icon     = R.drawable.ic_home,
                label    = "Inicio",
                selected = selectedTab == SolvyxBottomTab.INICIO,
                onClick  = { onTabSelected(SolvyxBottomTab.INICIO) },
                modifier = Modifier.weight(1f)
            )
            BottomNavTab(
                icon     = R.drawable.ic_plan,
                label    = "Plan",
                selected = selectedTab == SolvyxBottomTab.PLAN,
                onClick  = { onTabSelected(SolvyxBottomTab.PLAN) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.weight(1f))   // Berto slot
            BottomNavTab(
                icon     = R.drawable.ic_footsteps,
                label    = "Mi camino",
                selected = selectedTab == SolvyxBottomTab.AVANCES,
                onClick  = { onTabSelected(SolvyxBottomTab.AVANCES) },
                modifier = Modifier.weight(1f)
            )
        }

        // ── Berto elevated button ─────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = bertoXOffset),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(3.dp, Color.White, CircleShape)
                    .clickable { onTabSelected(SolvyxBottomTab.CHATBOT) },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.berto_cabeza),
                    contentDescription = "Berto",
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = "Berto",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selectedTab == SolvyxBottomTab.CHATBOT)
                        FontWeight.Bold else FontWeight.Normal
                ),
                color = if (selectedTab == SolvyxBottomTab.CHATBOT)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // ── SOS floating button ───────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-16).dp, y = (-60).dp)
        ) {
            SolvyxSosButton(onClick = onSosClick)
        }
    }
}

@Composable
private fun BottomNavTab(
    icon: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (selected) {
            Box(
                Modifier
                    .padding(top = 3.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun SolvyxSosButton(onClick: () -> Unit) {
    val sosRed = Color(0xFFE24B4A)
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(sosRed.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(sosRed)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SOS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                ),
                color = Color.White
            )
        }
    }
}
