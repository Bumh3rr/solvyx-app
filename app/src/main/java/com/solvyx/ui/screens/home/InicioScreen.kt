package com.solvyx.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.R
import com.solvyx.ui.components.dialog.SosConfirmationDialog
import com.solvyx.ui.components.drawer.model.CustomDrawerState
import com.solvyx.ui.screens.red.RedApoyoViewModel
import com.solvyx.ui.theme.WarnAmber
import com.solvyx.ui.theme.WarnAmberDark

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InicioScreen(
    onOpenDrawer: () -> Unit,
    drawerState: CustomDrawerState,
    onNavigateToRedApoyo: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToSos: () -> Unit = {},
    onNavigateToDirectorio: () -> Unit = {},
    onNavigateToEjercicio: () -> Unit = {},
    onNavigateToPlan: () -> Unit = {},
    onNavigateToRegistro: () -> Unit = {},
    onNavigateToGuias: () -> Unit = {},
    onNavigateToPsicoeducacion: () -> Unit = {},
    onNavigateToJournaling: () -> Unit = {},
    onNavigateToRutinas: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToGuiasExtendidas: () -> Unit = {},
    onNavigateToDescubrir: () -> Unit = {}
) {
    val viewModel: InicioViewModel = hiltViewModel()
    val redApoyoViewModel: RedApoyoViewModel = hiltViewModel()
    val contactCount = redApoyoViewModel.contactos.count { it.nombre.isNotBlank() }
    var showSosDialog by remember { mutableStateOf(false) }
    if (showSosDialog) {
        SosConfirmationDialog(
            onConfirm = { showSosDialog = false; onNavigateToSos() },
            onDismiss = { showSosDialog = false }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
            // ── Top bar ───────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenDrawer,
                    enabled = drawerState == CustomDrawerState.Closed
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_menu),
                        contentDescription = "Menú",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Solvyx",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(onClick = { }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_bell),
                            contentDescription = "Notificaciones",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE24B4A))
                            .align(Alignment.TopEnd)
                            .offset(x = (-6).dp, y = 6.dp)
                    )
                }
            }

            // ── Scrollable content ────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Hero section ──────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 20.dp)
                        .padding(top = 10.dp, bottom = 38.dp)
                ) {
                    // Formas decorativas de fondo (cuadros y círculos)
                    Image(
                        painter = painterResource(R.drawable.ic_header_hero),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp)
                            .align(Alignment.TopCenter),
                        contentScale = ContentScale.FillWidth,
                        alpha = 1f
                    )
                    Image(
                        painter = painterResource(R.drawable.berto_tranquilo),
                        contentDescription = "Berto",
                        modifier = Modifier
                            .size(130.dp)
                            .align(Alignment.CenterEnd)
                            .offset(y = 16.dp)
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(end = 140.dp)
                    ) {
                        Text(
                            text = "Hola, ${viewModel.apodo}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White
                        )
                        Text(
                            text = viewModel.fechaHoy,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.80f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.White)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_flame),
                                contentDescription = null,
                                tint = Color(0xFFE24B4A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${viewModel.racha} días de racha",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // ── Cards panel (overlap hero) ────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 8.dp)
                ) {
                    // ── SOS warning banner ────────────
                    AnimatedVisibility(
                        visible = contactCount == 0,
                        enter = fadeIn() + expandVertically(),
                        exit  = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            SosWarningBanner(onConfigClick = onNavigateToRedApoyo)
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    // ── Herramientas rápidas ─────────
                    Text(
                        "Herramientas rápidas",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HerramientaRapidaCard("Respirar",         R.drawable.ic_wind,           onNavigateToEjercicio)
                        HerramientaRapidaCard("Hablar con Berto", R.drawable.ic_chat,           onNavigateToChat)
                        HerramientaRapidaCard("Estoy en crisis",  R.drawable.ic_alert_triangle, { showSosDialog = true })
                        HerramientaRapidaCard("Buscar ayuda",     R.drawable.ic_building,       onNavigateToDirectorio)
                    }
                    Spacer(Modifier.height(16.dp))

                    // ── Carrusel de sugerencias ──────────
                    val sugerencias = listOf(
                        "Antes de consumir, toma agua y come algo primero.",
                        "Si sientes ganas de consumir, espera 15 minutos antes de decidir.",
                        "Habla con alguien de confianza antes de consumir.",
                        "Reduce la dosis a la mitad respecto a la última vez."
                    )
                    var sugerenciaIndex by remember { mutableIntStateOf(0) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_flag),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    "Sugerencia del día",
                                    Modifier
                                        .weight(1f)
                                        .padding(start = 10.dp),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    "${sugerenciaIndex + 1} / ${sugerencias.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.55f)
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                thickness = 0.5.dp
                            )
                            AnimatedContent(
                                targetState = sugerenciaIndex,
                                transitionSpec = {
                                    (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it } + fadeOut())
                                },
                                label = "SugerenciaCarousel"
                            ) { idx ->
                                Text(
                                    text = sugerencias[idx],
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.92f),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    sugerencias.indices.forEach { i ->
                                        Box(
                                            Modifier
                                                .size(if (i == sugerenciaIndex) 10.dp else 7.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (i == sugerenciaIndex) Color.White
                                                    else Color.White.copy(alpha = 0.30f)
                                                )
                                        )
                                    }
                                }
                                TextButton(
                                    onClick = {
                                        sugerenciaIndex = (sugerenciaIndex + 1) % sugerencias.size
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text(
                                        "Siguiente →",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ── ¿Cómo te sientes hoy? card ────
                    var emocionSeleccionada by remember { mutableStateOf<String?>(null) }
                    val emociones = listOf(
                        "Bien"      to R.drawable.ic_face_happy,
                        "Tranquilo" to R.drawable.ic_face_neutral,
                        "Ansioso"   to R.drawable.ic_face_anxious,
                        "Triste"    to R.drawable.ic_face_sad,
                        "Agotado"   to R.drawable.ic_face_tired
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceDim
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_heart_pulse),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "¿Cómo te sientes hoy?",
                                    Modifier
                                        .weight(1f)
                                        .padding(start = 6.dp),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "Registrado",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                emociones.forEach { (nombre, icono) ->
                                    val sel = emocionSeleccionada == nombre
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) { emocionSeleccionada = nombre }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (sel) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.primaryContainer
                                                )
                                                .then(
                                                    if (!sel) Modifier.border(
                                                        1.5.dp,
                                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                        CircleShape
                                                    ) else Modifier
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(icono),
                                                contentDescription = nombre,
                                                tint = if (sel) Color.White
                                                       else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            nombre,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (sel) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            AnimatedVisibility(
                                visible = emocionSeleccionada != null,
                                enter = fadeIn() + expandVertically(),
                                exit  = fadeOut() + shrinkVertically()
                            ) {
                                Column {
                                    Spacer(Modifier.height(12.dp))
                                    EmocionSugerenciaCard(
                                        emocion = emocionSeleccionada ?: "",
                                        onNavigateToChat = onNavigateToChat,
                                        onNavigateToEjercicio = onNavigateToEjercicio,
                                        onNavigateToPlan = onNavigateToPlan,
                                        onNavigateToRegistro = onNavigateToRegistro
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Tu registro es privado y nunca sale de este teléfono.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Accesos rápidos ───────────────
                    Text(
                        "Accesos rápidos",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))

                    class AccesoItem(val titulo: String, val subtitulo: String, val iconRes: Int, val usePrimary: Boolean = true, val onClick: () -> Unit = {})
                    val accesos = listOf(
                        AccesoItem("Mi Plan",           "Meta de hoy lista",       R.drawable.ic_target,      usePrimary = true,  onClick = onNavigateToPlan),
                        AccesoItem("Técnicas",          "Manejo y reducción",      R.drawable.ic_brain,       usePrimary = false, onClick = onNavigateToPlan),
                        AccesoItem("Hablar con Berto",  "Disponible ahora",        R.drawable.ic_chat,        usePrimary = true,  onClick = onNavigateToChat),
                        AccesoItem("Primeros Auxilios", "Sin conexión a internet", R.drawable.ic_guide,       usePrimary = false, onClick = onNavigateToGuias),
                        AccesoItem("Mi Registro",       "Ver mis patrones",        R.drawable.ic_trending_up, usePrimary = true,  onClick = onNavigateToRegistro),
                        AccesoItem("Mi Red de Apoyo",   "Contactos de confianza",  R.drawable.ic_people,      usePrimary = false, onClick = onNavigateToRedApoyo)
                    )
                    data class DescubreCard(
                        val titulo: String,
                        val subtitulo: String,
                        val iconRes: Int,
                        val category: com.solvyx.ui.components.common.AccessCategory,
                        val isNew: Boolean = false,
                        val onClick: () -> Unit
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        accesos.chunked(2).forEach { rowItems ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { item ->
                                    AccesoRapidoCard(
                                        titulo = item.titulo,
                                        subtitulo = item.subtitulo,
                                        iconRes = item.iconRes,
                                        iconContainerColor = if (item.usePrimary)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.secondaryContainer,
                                        onClick = item.onClick,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── DESCUBRE SOLVYX (NUEVO) ───────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Descubre Solvyx",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        androidx.compose.material3.TextButton(onClick = onNavigateToDescubrir) {
                            Text(
                                "Ver todo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Recursos nuevos para acompañarte.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    val descubriCards = listOf(
                        DescubreCard(
                            titulo = "Ejercicios",
                            subtitulo = "Respiración, body scan, grounding",
                            iconRes = R.drawable.ic_wind,
                            category = com.solvyx.ui.components.common.AccessCategory.Calm,
                            isNew = true,
                            onClick = onNavigateToEjercicio
                        ),
                        DescubreCard(
                            titulo = "Psicoeducación",
                            subtitulo = "24 lecciones por sustancia",
                            iconRes = R.drawable.ic_brain,
                            category = com.solvyx.ui.components.common.AccessCategory.Learn,
                            isNew = true,
                            onClick = onNavigateToPsicoeducacion
                        ),
                        DescubreCard(
                            titulo = "Rutinas",
                            subtitulo = "Matutina y nocturna",
                            iconRes = R.drawable.ic_calendar,
                            category = com.solvyx.ui.components.common.AccessCategory.Calm,
                            isNew = true,
                            onClick = onNavigateToRutinas
                        ),
                        DescubreCard(
                            titulo = "Journaling",
                            subtitulo = "Escribe con prompts o libre",
                            iconRes = R.drawable.ic_pencil,
                            category = com.solvyx.ui.components.common.AccessCategory.Express,
                            isNew = true,
                            onClick = onNavigateToJournaling
                        ),
                        DescubreCard(
                            titulo = "Guías extendidas",
                            subtitulo = "8 guías adicionales",
                            iconRes = R.drawable.ic_clipboard,
                            category = com.solvyx.ui.components.common.AccessCategory.Support,
                            isNew = true,
                            onClick = onNavigateToGuiasExtendidas
                        ),
                        DescubreCard(
                            titulo = "Insights",
                            subtitulo = "Patrones que Berto nota",
                            iconRes = R.drawable.ic_chart_bar,
                            category = com.solvyx.ui.components.common.AccessCategory.Learn,
                            isNew = true,
                            onClick = onNavigateToInsights
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        descubriCards.chunked(2).forEach { rowItems ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { card ->
                                    com.solvyx.ui.components.common.SolvyxAccessCard(
                                        title = card.titulo,
                                        description = card.subtitulo,
                                        iconRes = card.iconRes,
                                        category = card.category,
                                        isNew = card.isNew,
                                        onClick = card.onClick,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }

                }
            }
    }
}

@Composable
private fun SosWarningBanner(onConfigClick: () -> Unit) {
    val amberStroke = Color(0xFFd97706)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .border(0.5.dp, amberStroke.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .background(WarnAmber),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(amberStroke)
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_alert_triangle),
                contentDescription = null,
                tint = amberStroke,
                modifier = Modifier.size(20.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "Tu botón SOS no tiene contactos",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = WarnAmberDark
                )
                Text(
                    text = "Sin contactos, nadie recibirá aviso en una emergencia.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarnAmberDark.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                text = "Configurar →",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = amberStroke,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onConfigClick() }
            )
        }
    }
}

@Composable
private fun AccesoRapidoCard(
    titulo: String,
    subtitulo: String,
    iconRes: Int,
    iconContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceDim),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = titulo,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                titulo,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun HerramientaRapidaCard(
    titulo: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = titulo,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun EmocionSugerenciaCard(
    emocion: String,
    onNavigateToChat: () -> Unit,
    onNavigateToEjercicio: () -> Unit,
    onNavigateToPlan: () -> Unit,
    onNavigateToRegistro: () -> Unit
) {
    val iconRes = when (emocion) {
        "Bien"      -> R.drawable.ic_trending_up
        "Tranquilo" -> R.drawable.ic_trending_up
        "Ansioso"   -> R.drawable.ic_wind
        else        -> R.drawable.ic_chat
    }
    val mensaje = when (emocion) {
        "Bien"      -> "Buen momento para registrar tu día."
        "Tranquilo" -> "Buen momento para registrar cómo te sientes."
        "Ansioso"   -> "Prueba un ejercicio de respiración."
        else        -> "Berto puede escucharte en este momento."
    }
    val accion = when (emocion) {
        "Bien"      -> "Ir al registro"
        "Tranquilo" -> "Ir al registro"
        "Ansioso"   -> "Respirar ahora"
        else        -> "Hablar con Berto"
    }
    val onAccion: () -> Unit = when (emocion) {
        "Bien"      -> onNavigateToRegistro
        "Tranquilo" -> onNavigateToRegistro
        "Ansioso"   -> onNavigateToEjercicio
        else        -> onNavigateToChat
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
        Text(
            text = accion,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onAccion() }
        )
    }
}

