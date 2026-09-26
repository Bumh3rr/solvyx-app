package com.solvyx.ui.screens.directory.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.screens.directory.model.DirectoryCategory
import com.solvyx.ui.screens.directory.model.DirectoryEntry
import com.solvyx.ui.theme.DirectoryCenter
import com.solvyx.ui.theme.DirectoryHelpline
import com.solvyx.ui.theme.DirectoryPsychologist
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private const val BadgeTintAlpha = 0.12f
private const val IconToBadgeRatio = 0.48f

val DirectoryCategory.accent: Color
    get() = when (this) {
        DirectoryCategory.HELPLINE -> DirectoryHelpline
        DirectoryCategory.CENTER -> DirectoryCenter
        DirectoryCategory.PSYCHOLOGIST -> DirectoryPsychologist
    }

/** Category icon in a rounded capsule tinted with the category color. */
@Composable
fun CategoryIconBadge(
    category: DirectoryCategory,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(category.accent.copy(alpha = BadgeTintAlpha), RoundedCornerShape(size / 3.5f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(category.icon),
            contentDescription = null,
            tint = category.accent,
            modifier = Modifier.size(size * IconToBadgeRatio)
        )
    }
}

/** Visual weight of an [InfoPill]: highlighted facts (free, 24/7) vs neutral ones (cost, cita). */
enum class PillStyle { HIGHLIGHT, NEUTRAL }

/** Small fact about an entry: "Sin costo", "24/7", "Con cita previa", "Verificado"... */
@Composable
fun InfoPill(
    text: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    style: PillStyle = PillStyle.NEUTRAL
) {
    val (background, content) = when (style) {
        PillStyle.HIGHLIGHT -> MaterialTheme.colorScheme.primary.copy(alpha = BadgeTintAlpha) to
            MaterialTheme.colorScheme.primary
        PillStyle.NEUTRAL -> TealLight.copy(alpha = 0.35f) to TealDark
    }
    Row(
        modifier = modifier
            .background(background, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon?.let {
            Icon(
                painter = painterResource(it),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = content
        )
    }
}

/** The facts worth scanning in a list, in priority order. */
@Composable
fun EntryFactPills(entry: DirectoryEntry) {
    if (entry.available24h) InfoPill("24/7", icon = R.drawable.ic_clock, style = PillStyle.HIGHLIGHT)
    if (entry.isFree) InfoPill("Sin costo", style = PillStyle.HIGHLIGHT)
    entry.cost?.let { InfoPill(it, icon = R.drawable.ic_tag) }
    entry.appointment?.let { InfoPill(it, icon = R.drawable.ic_clock) }
    if (entry.isVerified) InfoPill("Verificado", icon = R.drawable.ic_check)
}
