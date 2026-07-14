package com.solvyx.ui.screens.guias.components

import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.theme.SolvyxappTheme
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLightest

@Preview(name = "Guia Top Bar — Light", showSystemUi = true)
@Composable
private fun GuiaTopBarLight() {
    SolvyxappTheme(darkTheme = false) {
        GuiaTopBar(
            title = "Guía",
            onBack = {},
            showBertoBadge = true,
            useDarkBg = false,
            isMenuButton = false
        )
    }
}

@Composable
fun GuiaTopBar(
    title: String,
    onBack: () -> Unit,
    showBertoBadge: Boolean = false,
    useDarkBg: Boolean = false,
    isMenuButton: Boolean = false
) {
    val bg = if (useDarkBg) TealDark else MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 26.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isMenuButton) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu),
                    contentDescription = "Menú",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
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

        if (showBertoBadge) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(TealLightest),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.berto_preocupado),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit
                )
            }
        } else {
            Spacer(Modifier.size(44.dp))
        }
    }
}

// ── Hero with Berto on right ─────────────────────────────────────────────────

@Composable
fun HeroSideBerto(
    @DrawableRes mascot: Int,
    title: String,
    subtitle: String,
    darkBg: Boolean = false
) {
    val bg = if (darkBg) TealDark else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = (-24).dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 56.dp),
        content = content
    )
}

// ── Border Card ──────────────────────────────────────────────────────────────
@Composable
fun BorderCard(
    modifier: Modifier = Modifier,
    bg: Color = MaterialTheme.colorScheme.surface,
    leftBorderColor: Color? = null,
    radius: Dp = 16.dp,
    pad: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)

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
                    .padding(
                        start = pad,
                        top = pad,
                        end = pad,
                        bottom = pad
                    ),
                content = content
            )
        }
    }
}

// ── Card Label (icon + bold title) ──────────────────────────────────────────

@Composable
fun CardLabel(
    @DrawableRes iconRes: Int,
    text: String,
    color: Color? = null,
    size: Int = 14
) {
    val c = color ?: MaterialTheme.colorScheme.primary
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
            fontSize = size.sp,
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
    text: String,
    textSize: Int = 13
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
            fontSize = textSize.sp,
            color = TealDark,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

// ── Help Line Row (tappable phone number) ────────────────────────────────────

@Composable
fun HelpLineRow(name: String, num: String) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                context.startActivity(
                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
                )
            }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 13.sp,
            color = TealDark,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(99.dp))
                .background(TealLightest)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = num,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
