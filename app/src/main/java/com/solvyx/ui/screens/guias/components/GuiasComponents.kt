package com.solvyx.ui.screens.guias.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.components.common.SolvyxMenuButton
import com.solvyx.ui.components.common.SolvyxTopBarButtonSize
import com.solvyx.ui.components.haze.LocalHazeState
import com.solvyx.ui.components.navigation.SolvyxBottomNavClearance
import com.solvyx.ui.theme.TealDark
import dev.chrisbanes.haze.haze

private val BorderCardRadius = 16.dp
private val BorderCardPadding = 14.dp
private val PanelOverlap = 24.dp

@Composable
fun GuiaTopBar(
    title: String,
    onBack: () -> Unit,
    isMenuButton: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp, vertical = 26.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isMenuButton) {
            SolvyxMenuButton(onClick = onBack)
        } else {
            SolvyxBackButton(onClick = onBack)
        }

        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.size(SolvyxTopBarButtonSize))
    }
}

// ── Hero with Berto on right ─────────────────────────────────────────────────

@Composable
fun HeroSideBerto(
    @DrawableRes mascot: Int,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 44.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 120.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.80f)
            )
        }

        Image(
            painter = painterResource(mascot),
            contentDescription = null,
            modifier = Modifier
                .size(110.dp)
                .align(Alignment.BottomEnd)
                .offset(y = 16.dp),
            contentScale = ContentScale.Fit
        )
    }
}

// ── Scrollable Panel (overlaps hero by 24dp) ─────────────────────────────────

@Composable
fun GuiaPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val hazeState = LocalHazeState.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .overlapAbove(PanelOverlap)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .haze(
                hazeState,
                backgroundColor = MaterialTheme.colorScheme.background,
                tint = MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
                blurRadius = 16.dp
            )
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = SolvyxBottomNavClearance),
        content = content
    )
}

/**
 * Pulls the panel [overlap] up over the hero *and* grows it by the same amount, so it still reaches
 * the bottom. A plain `offset` only moves the drawing, leaving an [overlap]-tall strip of the
 * screen background exposed at the bottom (visible as a green band behind the bottom nav).
 */
private fun Modifier.overlapAbove(overlap: Dp): Modifier = layout { measurable, constraints ->
    val overlapPx = overlap.roundToPx()
    val grown = if (constraints.hasBoundedHeight) {
        constraints.copy(
            minHeight = constraints.minHeight + overlapPx,
            maxHeight = constraints.maxHeight + overlapPx
        )
    } else {
        constraints
    }
    val placeable = measurable.measure(grown)
    layout(placeable.width, (placeable.height - overlapPx).coerceAtLeast(0)) {
        placeable.place(0, -overlapPx)
    }
}

// ── Border Card ──────────────────────────────────────────────────────────────
@Composable
fun BorderCard(
    modifier: Modifier = Modifier,
    bg: Color = MaterialTheme.colorScheme.surface,
    leftBorderColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(BorderCardRadius)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                shape = shape
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)   // clave: la fila toma la altura del contenido
        ) {
            // Borde izquierdo — mismo alto que el contenido
            if (leftBorderColor != null) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()     // rellena hasta donde llegue el contenido
                        .background(leftBorderColor)
                )
            }

            // Contenido
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(BorderCardPadding),
                content = content
            )
        }
    }
}

// ── Card Label (icon + bold title) ──────────────────────────────────────────

@Composable
fun CardLabel(
    @DrawableRes iconRes: Int,
    text: String
) {
    val c = MaterialTheme.colorScheme.primary
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = c,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TealDark
        )
    }
}

// ── Dot Row (bullet point list item) ─────────────────────────────────────────

@Composable
fun DotRow(
    color: Color? = null,
    textColor: Color = TealDark,
    text: String
) {
    val dotColor = color ?: MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(5.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = textColor,
            lineHeight = 20.sp
        )
    }
}

// ── Step Row (numbered step) ─────────────────────────────────────────────────

@Composable
fun StepRow(
    n: Int,
    text: String
) {
    Row(
        modifier = Modifier.padding(top = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = n.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = TealDark,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}
