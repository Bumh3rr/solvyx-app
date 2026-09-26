package com.solvyx.ui.components.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.haze.LocalHazeState
import dev.chrisbanes.haze.hazeChild

enum class SolvyxBottomTab { INICIO, PLAN, JOURNEY }

private val SolvyxBottomTab.slotIndex: Int
    get() = when (this) {
        SolvyxBottomTab.INICIO -> 0
        SolvyxBottomTab.PLAN -> 1
        SolvyxBottomTab.JOURNEY -> 2
    }

private const val TAB_COUNT = 3

private val BarHeight = 80.dp

/** How far the floating SOS button rises above the top edge of the bar. */
private val SosButtonLift = 60.dp
private val ClearanceBreathingRoom = 8.dp

/**
 * Bottom padding for content that scrolls behind the bottom nav (via [dev.chrisbanes.haze.haze]):
 * the bar plus the part of the SOS button that floats above it, so at the end of a scroll nothing
 * (e.g. a button on the right edge) stays hidden under SOS.
 */
val SolvyxBottomNavClearance: Dp = BarHeight + SosButtonLift + ClearanceBreathingRoom

@Composable
fun SolvyxBottomNavigationBar(
    selectedTab: SolvyxBottomTab,
    onTabSelected: (SolvyxBottomTab) -> Unit,
    onSosClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hazeState = LocalHazeState.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BarHeight)
    ) {
        BoxWithConstraints(
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
                )
        ) {
            // One shared dot that travels to whichever tab is selected, instead of each tab
            // drawing its own — the selection reads as movement across the bar, not a pop in a
            // new spot. Position is a fraction of the bar's own measured width (3 equal slots),
            // so it stays correct regardless of screen size.
            val dotSize = 5.dp
            val slotWidth = maxWidth / TAB_COUNT
            val targetDotX = slotWidth * selectedTab.slotIndex + slotWidth / 2 - dotSize / 2
            val dotX by animateDpAsState(
                targetValue = targetDotX,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "NavIndicatorX"
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = dotX, y = (-6).dp)
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavTab(
                    icon = R.drawable.ic_home,
                    label = "Inicio",
                    selected = selectedTab == SolvyxBottomTab.INICIO,
                    onClick = { onTabSelected(SolvyxBottomTab.INICIO) },
                    modifier = Modifier.weight(1f)
                )
                BottomNavTab(
                    icon = R.drawable.ic_plan,
                    label = "Plan",
                    selected = selectedTab == SolvyxBottomTab.PLAN,
                    onClick = { onTabSelected(SolvyxBottomTab.PLAN) },
                    modifier = Modifier.weight(1f)
                )
                BottomNavTab(
                    icon = R.drawable.ic_footsteps,
                    label = "Mi camino",
                    selected = selectedTab == SolvyxBottomTab.JOURNEY,
                    onClick = { onTabSelected(SolvyxBottomTab.JOURNEY) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── SOS floating button ─────────────────────── This one genuinely IS a one-off
        // action floating above the bar (not a peer destination like the tabs), so it keeps
        // its own overlay treatment.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-16).dp, y = -SosButtonLift)
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
    // Same "pop" language as the achievements' unlock celebration elsewhere in the app: a quick
    // bounce past 1x settling back to 1x, played once whenever this tab lands on "selected" —
    // gives the traveling dot's arrival a matching beat on the icon itself.
    val iconScale = remember { Animatable(1f) }
    LaunchedEffect(selected) {
        if (selected) {
            iconScale.animateTo(1.22f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            iconScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
    }

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
            modifier = Modifier
                .size(22.dp)
                .scale(iconScale.value)
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
        // No longer draws its own dot here — the shared traveling indicator above owns that job.
    }
}

@Composable
fun SolvyxSosButton(onClick: () -> Unit) {
    val sosRed = Color(0xFFE24B4A)
    val hazeState = LocalHazeState.current

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(sosRed.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(sosRed)
                .border(3.dp, Color.White, CircleShape)
                .hazeChild(
                    state = hazeState,
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_alert_triangle),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
