package com.solvyx.ui.screens.bitacora

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.haze.LocalHazeState
import com.solvyx.ui.components.navigation.SolvyxBottomNavHeight
import com.solvyx.ui.theme.SolvyxappTheme
import dev.chrisbanes.haze.haze
import java.time.format.DateTimeFormatter
import java.util.Locale

@Preview(name = "RegistroEmocional — Light", showSystemUi = true)
@Composable
private fun RegistroEmocionalPreview() {
    SolvyxappTheme(darkTheme = false) {
        RegistroEmocionalScreen(onOpenDrawer = {})
    }
}

@Composable
fun RegistroEmocionalScreen(
    onOpenDrawer: () -> Unit,
    viewModel: RegistroViewModel = hiltViewModel()
) {
    var showHistorial by remember { mutableStateOf(false) }

    if (showHistorial) {
        HistorialBitacoraScreen(viewModel = viewModel, onBack = { showHistorial = false })
        return
    }

    if (viewModel.isSaved) {
        RegistroExitosoDialog(
            estadoAnimo = viewModel.estadoAnimo,
            consumio = viewModel.consumo == true,
            sustancia = viewModel.sustanciaSeleccionada,
            onDismiss = { viewModel.resetForm() }
        )
    }

    if (viewModel.showCalendar) {
        CalendarBottomSheet(
            fechaSeleccionada = viewModel.fechaSeleccionada,
            fechasConRegistro = viewModel.fechasConRegistro.collectAsState().value,
            onFechaSelected = {
                viewModel.setFecha(it)
                viewModel.toggleCalendar()
            },
            onDismiss = { viewModel.toggleCalendar() }
        )
    }

    if (viewModel.showSustanciaSheet) {
        SustanciaBottomSheet(
            sustanciaSeleccionada = viewModel.sustanciaSeleccionada,
            onSustanciaSelected = {
                viewModel.setSustancia(it)
                viewModel.toggleSustanciaSheet()
            },
            onDismiss = { viewModel.toggleSustanciaSheet() }
        )
    }

    val hazeState = LocalHazeState.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .haze(
                hazeState,
                backgroundColor = MaterialTheme.colorScheme.background,
                tint = MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
                blurRadius = 16.dp
            )
            .background(MaterialTheme.colorScheme.background)
    ) {
            // ── Top bar ─────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
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
                    "Bitácora Diaria",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { showHistorial = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_history),
                        contentDescription = "Historial",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // ── Scrollable content ───────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 48.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.berto_saludando),
                        contentDescription = "Berto",
                        modifier = Modifier
                            .size(110.dp)
                            .align(Alignment.CenterEnd)
                            .offset(y = 12.dp)
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(end = 120.dp)
                    ) {
                        Text(
                            "HOY ES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = Color.White.copy(alpha = 0.70f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            viewModel.fechaSeleccionada.format(
                                DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es"))
                            ).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.White)
                                .clickable { viewModel.toggleCalendar() }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_calendar),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Cambiar fecha",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_down),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Cards panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-20).dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 8.dp)
                ) {
                    // Card 1: Estado de ánimo
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceDim
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_heart),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                Text(
                                    "¿Cómo te sientes hoy?",
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 10.dp),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "Obligatorio",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            Text(
                                "Sé honesto contigo mismo. Nadie más va a ver esto.",
                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp, start = 2.dp)
                            )
                            Spacer(Modifier.height(16.dp))

                            val emociones = listOf(
                                "triste"   to R.drawable.ic_face_sad,
                                "ansioso"  to R.drawable.ic_face_anxious,
                                "neutral"  to R.drawable.ic_face_neutral,
                                "bien"     to R.drawable.ic_face_happy,
                                "euforico" to R.drawable.ic_face_euphoric
                            )
                            val emoLabels = listOf("Triste", "Ansioso", "Neutral", "Bien", "Eufórico")
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                emociones.forEachIndexed { idx, (id, icono) ->
                                    val sel = viewModel.estadoAnimo == id
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) { viewModel.updateEstadoAnimo(id) }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .then(
                                                    if (sel) Modifier.border(
                                                        2.dp,
                                                        MaterialTheme.colorScheme.primary,
                                                        CircleShape
                                                    )
                                                    else Modifier
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(icono),
                                                contentDescription = emoLabels[idx],
                                                tint = if (sel) MaterialTheme.colorScheme.primary
                                                       else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                        Text(
                                            emoLabels[idx],
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (sel) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 6.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            OutlinedTextField(
                                value = viewModel.notaAnimo,
                                onValueChange = { viewModel.updateNotaAnimo(it) },
                                placeholder = {
                                    Text(
                                        "Agrega una nota sobre cómo te sientes (opcional)...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 80.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                    focusedContainerColor = MaterialTheme.colorScheme.background
                                ),
                                suffix = {
                                    Text(
                                        "${viewModel.notaAnimo.length}/100",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                maxLines = 4
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Card 2: Consumo
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceDim),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_activity),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                Text(
                                    "¿Consumiste alguna sustancia hoy?",
                                    modifier = Modifier.padding(start = 6.dp),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val noSel = viewModel.consumo == false
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (noSel) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.background
                                        )
                                        .border(
                                            width = if (noSel) 2.dp else 1.dp,
                                            color = if (noSel) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { viewModel.updateConsumo(false) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_circle_x),
                                            contentDescription = "No",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            "No",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                val siSel = viewModel.consumo == true
                                val sosRed = Color(0xFFE24B4A)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (siSel) sosRed.copy(alpha = 0.08f)
                                            else MaterialTheme.colorScheme.background
                                        )
                                        .border(
                                            width = if (siSel) 2.dp else 1.dp,
                                            color = if (siSel) sosRed else MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { viewModel.updateConsumo(true) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_alert_circle),
                                            contentDescription = "Sí",
                                            tint = sosRed,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            "Sí",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = sosRed
                                        )
                                    }
                                }
                            }

                            if (viewModel.consumo == true && viewModel.sustanciaSeleccionada != null) {
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable { viewModel.toggleSustanciaSheet() }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        viewModel.sustanciaSeleccionada!!.replaceFirstChar { it.uppercase() },
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.ic_chevron_right),
                                        contentDescription = "Cambiar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Esta información nunca sale de tu teléfono",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Guardar (sticky, no scrollable, deja espacio para el bottom nav) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp)
                    .padding(top = 14.dp, bottom = SolvyxBottomNavHeight),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SolvyxButton(
                    text = "Guardar Registro",
                    onClick = { viewModel.guardarRegistro() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = viewModel.canSave(),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_save),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
                Text(
                    "Tu registro se guarda solo en este dispositivo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
    }
}

@Composable
private fun RegistroExitosoDialog(
    estadoAnimo: String?,
    consumio: Boolean,
    sustancia: String?,
    onDismiss: () -> Unit
) {
    val faceIcons = mapOf(
        "triste"   to R.drawable.ic_face_sad,
        "ansioso"  to R.drawable.ic_face_anxious,
        "neutral"  to R.drawable.ic_face_neutral,
        "bien"     to R.drawable.ic_face_happy,
        "euforico" to R.drawable.ic_face_euphoric
    )
    val emoLabels = mapOf(
        "triste" to "Triste", "ansioso" to "Ansioso",
        "neutral" to "Neutral", "bien" to "Bien", "euforico" to "Eufórico"
    )
    val mensaje = when (estadoAnimo) {
        "bien"     -> "¡Qué bueno escuchar eso!\nSigue cuidándote así."
        "euforico" -> "¡Qué energía! Aprovéchala\ncon sabiduría."
        "neutral"  -> "Un día tranquilo también\ncuenta. Gracias por registrar."
        "triste"   -> "Registrar cómo te sientes\ntoma valentía. Bien hecho."
        "ansioso"  -> "Reconocer la ansiedad\nes el primer paso. ¡Lo lograste!"
        else       -> "Gracias por ser honesto\ncontigo mismo hoy."
    }
    val sosRed = Color(0xFFE24B4A)

    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.75f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "DialogScale"
    )
    LaunchedEffect(Unit) { visible = true }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .scale(scale),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box {
                Column {
                    // ── Header teal ───────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(164.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.berto_feliz),
                            contentDescription = null,
                            modifier = Modifier
                                .size(130.dp)
                                .offset(y = (-8).dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // ── Body ──────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(30.dp))

                        Text(
                            "¡Registro guardado!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            mensaje,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(20.dp))

                        // ── Resumen ───────────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                .padding(vertical = 14.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ánimo
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(
                                        faceIcons[estadoAnimo] ?: R.drawable.ic_face_neutral
                                    ),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    emoLabels[estadoAnimo] ?: "—",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                Modifier
                                    .width(0.5.dp)
                                    .height(36.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            )

                            // Consumo
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(
                                        if (consumio) R.drawable.ic_alert_circle
                                        else R.drawable.ic_check_circle
                                    ),
                                    contentDescription = null,
                                    tint = if (consumio) sosRed
                                           else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    if (consumio)
                                        sustancia?.replaceFirstChar { it.uppercase() } ?: "Sí"
                                    else "Sin consumo",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        SolvyxButton(
                            text = "¡Todo listo!",
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(24.dp))
                    }
                }

                // ── Badge checkmark solapando el header ───
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = 140.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
