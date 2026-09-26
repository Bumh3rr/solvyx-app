package com.solvyx.ui.components.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.theme.TealDark

/** Side of the square top-bar button. Use it to size the balancing spacer on the opposite side. */
val SolvyxTopBarButtonSize = 44.dp

private val TopBarButtonCorner = 14.dp
private val BackIconSize = 20.dp
private val MenuIconSize = 22.dp
private const val OnPrimaryBackgroundAlpha = 0.15f
private const val OnSurfaceBackgroundAlpha = 0.10f

/** Which kind of surface the button sits on — decides its translucent fill and icon tint. */
enum class SolvyxTopBarButtonStyle {
    /** Over teal headers: translucent white square, white icon. */
    OnPrimary,

    /** Over light backgrounds (Home, wizards): translucent teal square, dark teal icon. */
    OnSurface
}

/**
 * The app's square icon button for top bars (back, menu, and any other header action).
 * Prefer [SolvyxBackButton] / [SolvyxMenuButton]; use this directly only for other header icons.
 */
@Composable
fun SolvyxTopBarButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SolvyxTopBarButtonStyle = SolvyxTopBarButtonStyle.OnPrimary,
    enabled: Boolean = true,
    iconSize: Dp = BackIconSize
) {
    val (background, tint) = when (style) {
        SolvyxTopBarButtonStyle.OnPrimary ->
            Color.White.copy(alpha = OnPrimaryBackgroundAlpha) to Color.White
        SolvyxTopBarButtonStyle.OnSurface ->
            MaterialTheme.colorScheme.primary.copy(alpha = OnSurfaceBackgroundAlpha) to TealDark
    }
    Box(
        modifier = modifier
            .size(SolvyxTopBarButtonSize)
            .clip(RoundedCornerShape(TopBarButtonCorner))
            .background(background)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun SolvyxBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SolvyxTopBarButtonStyle = SolvyxTopBarButtonStyle.OnPrimary
) {
    SolvyxTopBarButton(
        iconRes = R.drawable.ic_arrow_left,
        contentDescription = "Volver",
        onClick = onClick,
        modifier = modifier,
        style = style,
        iconSize = BackIconSize
    )
}

@Composable
fun SolvyxMenuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SolvyxTopBarButtonStyle = SolvyxTopBarButtonStyle.OnPrimary,
    enabled: Boolean = true
) {
    SolvyxTopBarButton(
        iconRes = R.drawable.ic_menu,
        contentDescription = "Abrir menú",
        onClick = onClick,
        modifier = modifier,
        style = style,
        enabled = enabled,
        iconSize = MenuIconSize
    )
}
