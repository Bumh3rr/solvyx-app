package com.solvyx.ui.screens.firstaid.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.solvyx.ui.screens.firstaid.model.GuideTone
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.CrisisRedDark
import com.solvyx.ui.theme.CrisisRedLight
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.WarnAmber
import com.solvyx.ui.theme.WarnAmberDark

private const val BadgeTintAlpha = 0.14f
private const val CalmBadgeAlpha = 0.45f

/**
 * Colors for a [GuideTone]. [container] is null for CALM so cards keep the default
 * `SolvyxCard` surface; tinted tones get their own background.
 */
data class GuideToneColors(
    val container: Color?,
    val accent: Color,
    val content: Color,
    val badge: Color
)

@Composable
fun GuideTone.colors(): GuideToneColors = when (this) {
    GuideTone.CALM -> GuideToneColors(
        container = null,
        accent = MaterialTheme.colorScheme.primary,
        content = TealDark,
        badge = TealLight.copy(alpha = CalmBadgeAlpha)
    )
    GuideTone.WARNING -> GuideToneColors(
        container = WarnAmber,
        accent = WarnAmberDark,
        content = WarnAmberDark,
        badge = WarnAmberDark.copy(alpha = BadgeTintAlpha)
    )
    GuideTone.URGENT -> GuideToneColors(
        container = CrisisRedLight,
        accent = CrisisRed,
        content = CrisisRedDark,
        badge = CrisisRed.copy(alpha = BadgeTintAlpha)
    )
}

/** Icon inside a rounded, tone-tinted capsule — the leading visual of first-aid cards. */
@Composable
fun ToneIconBadge(
    @DrawableRes icon: Int,
    tone: GuideTone,
    modifier: Modifier = Modifier
) {
    val colors = tone.colors()
    Box(
        modifier = modifier
            .size(42.dp)
            .background(colors.badge, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(20.dp)
        )
    }
}
