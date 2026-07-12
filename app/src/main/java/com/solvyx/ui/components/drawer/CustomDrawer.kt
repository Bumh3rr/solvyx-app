package com.solvyx.ui.components.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.components.drawer.model.NavigationItem
import com.solvyx.ui.components.drawer.model.isHerramientas
import com.solvyx.ui.components.drawer.model.isMiCuenta
import com.solvyx.ui.components.drawer.model.isRutina
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealMedium

/**
 * Drawer lateral de Solvyx.
 *
 * Organización de las 3 secciones (mantenidas por simplicidad):
 *
 * - **RUTINA** (5): uso diario, lo más frecuente
 * - **HERRAMIENTAS** (8): apoyo, recursos, descubrimiento
 * - **MI CUENTA** (2): perfil y sesión
 *
 * El item "Guías de primeros auxilios" se renderiza como submenú
 * expandible que muestra las 5 guías originales + 8 extendidas
 * marcadas con badge NUEVO.
 */
@Composable
fun CustomDrawer(
    selectedNavigationItem: NavigationItem,
    userNickname: String,
    onNavigationItemClick: (NavigationItem) -> Unit,
    onCloseClick: () -> Unit,
    onProfileClick: () -> Unit,
    onGuiaOriginalClick: (slug: String) -> Unit = {},
    onGuiaExtendidaClick: (slug: String) -> Unit = {},
    showNewBadges: Boolean = true,
    selectedGuiaSlug: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(fraction = 0.72f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(16.dp))

            SolvyxBackButton(onClick = onCloseClick)

            Spacer(Modifier.height(16.dp))

            // ── Wordmark ──────────────────────────────────
            Text(
                text = "Solvyx",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic
                ),
                color = Color.White
            )
            Text(
                text = "Tu mente, tu red, tu libertad",
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = Color.White.copy(alpha = 0.75f)
            )

            Spacer(Modifier.height(14.dp))

            // ── Tarjeta de perfil ─────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    .clickable { onProfileClick() }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TealMedium.copy(alpha = 0.3f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.berto_saludando),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hola, $userNickname",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Ver mi perfil →",
                        style = MaterialTheme.typography.bodySmall,
                        color = TealLight,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── RUTINA ────────────────────────────────────
            DrawerSectionHeader("RUTINA · uso diario")
            NavigationItem.entries
                .filter { it.isRutina() }
                .forEach { item ->
                    NavigationItemView(
                        navigationItem = item,
                        selected = item == selectedNavigationItem,
                        showNewBadge = showNewBadges,
                        onClick = { onNavigationItemClick(item) }
                    )
                    Spacer(Modifier.height(2.dp))
                }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))

            // ── HERRAMIENTAS ──────────────────────────────
            DrawerSectionHeader("HERRAMIENTAS · apoyo y recursos")
            // Renderiza el submenú de Guías + el resto de items
            NavigationItem.entries
                .filter { it.isHerramientas() }
                .forEach { item ->
                    when (item) {
                        NavigationItem.GuiasPrimerosAuxilios -> {
                            // Submenú expandible
                            GuiaSubmenu(
                                expandedByDefault = selectedGuiaSlug != null,
                                onToggle = { onNavigationItemClick(item) },
                                onOriginalClick = onGuiaOriginalClick,
                                onExtendidaClick = onGuiaExtendidaClick,
                                selectedSlug = selectedGuiaSlug
                            )
                        }
                        else -> {
                            NavigationItemView(
                                navigationItem = item,
                                selected = item == selectedNavigationItem,
                                showNewBadge = showNewBadges,
                                onClick = { onNavigationItemClick(item) }
                            )
                            Spacer(Modifier.height(2.dp))
                        }
                    }
                }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            // ── MI CUENTA ─────────────────────────────────
            DrawerSectionHeader("MI CUENTA")
            NavigationItem.entries
                .filter { it.isMiCuenta() }
                .forEach { item ->
                    NavigationItemView(
                        navigationItem = item,
                        selected = item == selectedNavigationItem,
                        isDestructive = item == NavigationItem.CerrarSesion,
                        showNewBadge = false,
                        onClick = { onNavigationItemClick(item) }
                    )
                    Spacer(Modifier.height(2.dp))
                }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DrawerSectionHeader(label: String) {
    Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = TealLight.copy(alpha = 0.7f),
        letterSpacing = 1.0.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Preview(showBackground = false, widthDp = 320, heightDp = 720, name = "CustomDrawer")
@Composable
private fun CustomDrawerPreview() {
    CustomDrawer(
        selectedNavigationItem = NavigationItem.Inicio,
        userNickname = "Alex",
        onNavigationItemClick = {},
        onCloseClick = {},
        onProfileClick = {}
    )
}
