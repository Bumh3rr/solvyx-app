package com.solvyx.ui.screens.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.screens.guias.components.BorderCard
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealLightest
import com.solvyx.ui.theme.TealMedium

@Composable
fun MiPerfilScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToAssist: () -> Unit,
    onNavigateToRedApoyo: () -> Unit,
    onNavigateToPrivacidad: () -> Unit,
    onNavigateToAcercaDe: () -> Unit,
    onNavigateToTerminos: () -> Unit,
    onLogout: () -> Unit,
    viewModel: PerfilViewModel = hiltViewModel()
) {
    if (viewModel.showEditarPerfil) {
        EditarPerfilBottomSheet(viewModel)
    }
    if (viewModel.showEditarSustancias) {
        EditarSustanciasBottomSheet(viewModel)
    }
    if (viewModel.showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = {
                viewModel.cerrarLogoutDialog()
                onLogout()
            },
            onDismiss = { viewModel.cerrarLogoutDialog() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top Bar ───────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TealDark)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu),
                    contentDescription = "Menú",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Mi Perfil",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            IconButton(onClick = { viewModel.abrirEditarPerfil() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_pencil),
                    contentDescription = "Editar perfil",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero (TealDark) ───────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TealDark)
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp, bottom = 56.dp),
                contentAlignment = Alignment.Center
            ) {
                PerfilConcentricRings()

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar Berto + punto activo
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(TealLightest)
                                .border(3.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.berto_saludando),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(84.dp)
                                    .offset(y = 6.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5DEFAB))
                                .border(2.5.dp, TealDark, CircleShape)
                                .offset(x = (-2).dp, y = (-4).dp)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = viewModel.apodo,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.01).sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Miembro desde ${viewModel.fechaRegistro}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.70f),
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    // Privacy pill
                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_shield),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Modo anónimo · Tus datos son tuyos",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            // ── Panel blanco (overlap -28dp) ──────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-28).dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 20.dp)
            ) {
                Spacer(Modifier.height(12.dp))

                // ── Stats Strip ───────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(0.5.dp, TealLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatCol(
                            iconRes = R.drawable.ic_flame,
                            iconColor = Color(0xFFE24B4A),
                            num = viewModel.rachaActual.toString(),
                            label = "Días de racha",
                            modifier = Modifier.weight(1f)
                        )
                        VerticalDivider(
                            modifier = Modifier.height(44.dp),
                            thickness = 1.dp,
                            color = TealLightest
                        )
                        StatCol(
                            iconRes = R.drawable.ic_trophy,
                            iconColor = TealMedium,
                            num = viewModel.mejorRacha.toString(),
                            label = "Mejor racha",
                            modifier = Modifier.weight(1f)
                        )
                        VerticalDivider(
                            modifier = Modifier.height(44.dp),
                            thickness = 1.dp,
                            color = TealLightest
                        )
                        StatCol(
                            iconRes = R.drawable.ic_clipboard,
                            iconColor = TealMedium,
                            num = viewModel.diagnosticosCompletados.toString(),
                            label = "Diagnósticos",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Sustancias en seguimiento ─────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(0.5.dp, TealLight)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_activity),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Sustancias en seguimiento",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = { viewModel.abrirEditarSustancias() },
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_pencil),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Cambiar",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = TealLightest,
                            thickness = 0.5.dp
                        )

                        SustanciasChips(viewModel)

                        Text(
                            text = "Puedes cambiar esto en cualquier momento",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic
                            ),
                            color = TealMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Nivel de riesgo ASSIST ────────────
                BorderCard(
                    leftBorderColor = viewModel.colorNivel(),
                    bg = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ASSIST · OMS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.04.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(viewModel.bgColorNivel())
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = viewModel.nivelRiesgo,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.06.sp
                                ),
                                color = viewModel.colorNivel()
                            )
                        }
                    }

                    Text(
                        text = "Riesgo ${viewModel.nivelRiesgo.lowercase()} detectado",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Text(
                        text = "Última evaluación: ${viewModel.fechaUltimoAssist}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TealMedium,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    LinearProgressIndicator(
                        progress = { viewModel.progresoRiesgo() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "0 · Bajo",
                            style = MaterialTheme.typography.labelSmall,
                            color = TealMedium
                        )
                        Text(
                            text = "27+ · Alto",
                            style = MaterialTheme.typography.labelSmall,
                            color = TealMedium
                        )
                    }

                    SolvyxOutlinedButton(
                        text = "Repetir diagnóstico",
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_refresh),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        onClick = onNavigateToAssist,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ── Settings List ─────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(0.5.dp, TealLight)
                ) {
                    Column {
                        SettingsRow(
                            iconRes = R.drawable.ic_bell,
                            label = "Notificaciones",
                            showDivider = true,
                            trailing = {
                                Switch(
                                    checked = viewModel.notificacionesActivas,
                                    onCheckedChange = { viewModel.toggleNotificaciones() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = TealLightest
                                    )
                                )
                            }
                        )
                        SettingsRow(
                            iconRes = R.drawable.ic_people,
                            label = "Mi Red de Apoyo",
                            showDivider = true,
                            onClick = onNavigateToRedApoyo,
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${viewModel.cantidadContactos} contactos",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TealLight
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        painter = painterResource(R.drawable.ic_chevron_right),
                                        contentDescription = null,
                                        tint = TealLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        )
                        SettingsRow(
                            iconRes = R.drawable.ic_shield,
                            label = "Privacidad y datos",
                            showDivider = true,
                            onClick = onNavigateToPrivacidad,
                            trailing = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_chevron_right),
                                    contentDescription = null,
                                    tint = TealLight,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                        SettingsRow(
                            iconRes = R.drawable.ic_info_circle,
                            label = "Acerca de Solvyx",
                            showDivider = true,
                            onClick = onNavigateToAcercaDe,
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "v1.0.0",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TealLight
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        painter = painterResource(R.drawable.ic_chevron_right),
                                        contentDescription = null,
                                        tint = TealLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        )
                        SettingsRow(
                            iconRes = R.drawable.ic_clipboard,
                            label = "Términos y condiciones",
                            showDivider = false,
                            onClick = onNavigateToTerminos,
                            trailing = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_chevron_right),
                                    contentDescription = null,
                                    tint = TealLight,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ── Cerrar sesión ─────────────────────
                SolvyxOutlinedButton(
                    text = "Cerrar sesión",
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_logout),
                            contentDescription = null,
                            tint = Color(0xFFE24B4A),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = { viewModel.abrirLogoutDialog() },
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Color(0xFFE24B4A),
                    textColor = Color(0xFFE24B4A)
                )

                Text(
                    text = "Tus datos siguen guardados en este dispositivo",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic
                    ),
                    color = TealMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )
                Text(
                    text = "Solvyx 2026 · Tecnologías para la Salud Humana",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.04.sp
                    ),
                    color = TealLight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                )
            }
        }
    }
}

// ── Chips de sustancias ───────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SustanciasChips(viewModel: PerfilViewModel) {
    val sustancias = listOf(
        "alcohol" to "Alcohol",
        "vape"    to "Vape",
        "cristal" to "Cristal",
        "tabaco"  to "Tabaco"
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sustancias.forEach { (id, label) ->
            val activa = viewModel.sustanciasSeleccionadas.contains(id)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        if (activa) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                    .clickable { viewModel.toggleSustancia(id) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (activa) FontWeight.ExtraBold else FontWeight.SemiBold
                    ),
                    color = if (activa) Color.White else TealMedium
                )
            }
        }
    }
}

// ── Anillos decorativos del hero ──────────────────────

@Composable
private fun PerfilConcentricRings() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        listOf(260.dp, 190.dp, 120.dp, 60.dp).forEach { r ->
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = r.toPx(),
                center = Offset(cx, cy),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}

// ── Columna de estadística ────────────────────────────
@Composable
private fun StatCol(
    iconRes: Int,
    iconColor: Color,
    num: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = num,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-0.02).sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TealMedium,
            modifier = Modifier.padding(top = 4.dp),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

// ── Fila de configuración ─────────────────────────────

@Composable
private fun SettingsRow(
    iconRes: Int,
    label: String,
    showDivider: Boolean,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = TealMedium,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            trailing()
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = TealLightest,
                thickness = 0.5.dp
            )
        }
    }
}

// ── Bottom Sheet: Editar perfil ───────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditarPerfilBottomSheet(viewModel: PerfilViewModel) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { viewModel.cerrarEditarPerfil() },
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.berto_saludando),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Editar perfil",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Los cambios se guardan en tu dispositivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = TealMedium
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Apodo",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TealDark,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = viewModel.apodoEditando,
                onValueChange = { viewModel.onApodoChange(it) },
                placeholder = { Text("Ej: Alex, Mia, Riku…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                ),
                supportingText = {
                    Text(
                        text = "${viewModel.apodoEditando.length}/30",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelSmall,
                        color = TealMedium
                    )
                }
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Fecha de nacimiento",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TealDark,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = viewModel.fechaNacimientoEditando,
                onValueChange = { viewModel.onFechaNacimientoChange(it) },
                placeholder = { Text("DD/MM/AAAA") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                ),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar),
                        contentDescription = null,
                        tint = TealMedium,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            Spacer(Modifier.height(24.dp))

            SolvyxButton(
                text = "Guardar cambios",
                onClick = { viewModel.guardarPerfil() },
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.apodoEditando.isNotBlank()
            )
        }
    }
}

// ── Bottom Sheet: Editar sustancias ───────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditarSustanciasBottomSheet(viewModel: PerfilViewModel) {
    ModalBottomSheet(
        onDismissRequest = { viewModel.cerrarEditarSustancias() },
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Sustancias en seguimiento",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Selecciona las que aplican",
                style = MaterialTheme.typography.bodySmall,
                color = TealMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            val sustancias = listOf(
                "alcohol" to "Alcohol",
                "vape"    to "Vape",
                "cristal" to "Cristal",
                "tabaco"  to "Tabaco"
            )
            sustancias.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { (id, label) ->
                        val activa = viewModel.sustanciasSeleccionadas.contains(id)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (activa) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    width = if (activa) 2.dp else 0.5.dp,
                                    color = if (activa) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.toggleSustancia(id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (activa) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_check),
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (activa) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(8.dp))

            SolvyxButton(
                text = "Listo",
                onClick = { viewModel.cerrarEditarSustancias() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── Diálogo: Confirmar cierre de sesión ───────────────

@Composable
private fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFfde8e8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_logout),
                        contentDescription = null,
                        tint = Color(0xFFE24B4A),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "¿Cerrar sesión?",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Tus datos seguirán guardados en este dispositivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TealMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 22.dp)
                )

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE24B4A)
                    )
                ) {
                    Text(
                        text = "Sí, cerrar sesión",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(10.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TealMedium
                    )
                }
            }
        }
    }
}
