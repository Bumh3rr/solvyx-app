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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.components.drawer.model.NavigationItem
import com.solvyx.ui.components.drawer.model.isBottom
import com.solvyx.ui.components.drawer.model.isPrimary
import com.solvyx.ui.components.drawer.model.isSecondary
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealMedium
import com.solvyx.ui.theme.TealPrimary

@Composable
fun CustomDrawer(
    selectedNavigationItem: NavigationItem,
    userNickname: String,
    onNavigationItemClick: (NavigationItem) -> Unit,
    onCloseClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(fraction = 0.63f)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 25.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Botón cerrar ──────────────────────────────
            SolvyxBackButton(onClick = onCloseClick)

            Spacer(Modifier.height(20.dp))

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
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic
                ),
                color = Color.White.copy(alpha = 0.75f)
            )

            Spacer(Modifier.height(20.dp))

            // ── Card de perfil ────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    .clickable { onProfileClick() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TealMedium.copy(alpha = 0.3f))
                        .border(2.dp, Color.White, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.berto_saludando),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hola, $userNickname 👋",
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

            Spacer(Modifier.height(24.dp))

            // ── Ítems primarios ───────────────────────────
            NavigationItem.entries
                .filter { it.isPrimary() }
                .forEach { item ->
                    NavigationItemView(
                        navigationItem = item,
                        selected = item == selectedNavigationItem,
                        onClick = { onNavigationItemClick(item) }
                    )
                    Spacer(Modifier.height(2.dp))
                }

            Spacer(Modifier.height(8.dp))

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.15f),
                thickness = 0.5.dp
            )

            Spacer(Modifier.height(8.dp))

            // ── Ítems secundarios ─────────────────────────
            NavigationItem.entries
                .filter { it.isSecondary() }
                .forEach { item ->
                    NavigationItemView(
                        navigationItem = item,
                        selected = item == selectedNavigationItem,
                        onClick = { onNavigationItemClick(item) }
                    )
                    Spacer(Modifier.height(2.dp))
                }

            Spacer(Modifier.weight(1f))

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.15f),
                thickness = 0.5.dp
            )

            Spacer(Modifier.height(8.dp))

            // ── Cerrar sesión ─────────────────────────────
            NavigationItem.entries
                .filter { it.isBottom() }
                .forEach { item ->
                    NavigationItemView(
                        navigationItem = item,
                        selected = false,
                        isDestructive = true,
                        onClick = { onNavigationItemClick(item) }
                    )
                }

            Spacer(Modifier.height(20.dp))
        }
    }
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

