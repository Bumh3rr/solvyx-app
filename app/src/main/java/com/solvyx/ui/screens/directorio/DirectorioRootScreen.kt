@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.solvyx.ui.screens.directorio

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.R
import com.solvyx.ui.screens.guias.components.GuiaTopBar
import com.solvyx.ui.screens.guias.components.HeroSideBerto
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight
import com.solvyx.ui.theme.TealLightest
import com.solvyx.ui.theme.TealMedium
import com.solvyx.ui.theme.TealPrimary

// ── Color helpers ─────────────────────────────────────

private fun colorBadgeTipo(tipo: TipoDirectorio): Color = when (tipo) {
    TipoDirectorio.CIJ       -> Color(0xFF1D9E75)
    TipoDirectorio.CLINICA   -> Color(0xFF5B8DEF)
    TipoDirectorio.PSICOLOGO -> Color(0xFF92400E)
    TipoDirectorio.LINEA     -> Color(0xFFEF6C5B)
}

private fun fondoBadgeTipo(tipo: TipoDirectorio): Color = when (tipo) {
    TipoDirectorio.CIJ       -> Color(0xFFE1F5EE)
    TipoDirectorio.CLINICA   -> Color(0xFFEEF3FD)
    TipoDirectorio.PSICOLOGO -> Color(0xFFFEF9C3)
    TipoDirectorio.LINEA     -> Color(0xFFFEF1EE)
}

// ── Root ──────────────────────────────────────────────

@Composable
fun DirectorioRootScreen(
    modifier: Modifier = Modifier,
    onOpenDrawer: () -> Unit,
    onNavigateToChat: () -> Unit,
    viewModel: DirectorioViewModel = hiltViewModel()
) {
    var selectedEntry by remember { mutableStateOf<EntradaDirectorio?>(null) }
    val listState = rememberLazyListState()

    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = selectedEntry,
            transitionSpec = {
                fadeIn(tween(350)) togetherWith fadeOut(tween(250))
            },
            label = "directorio_nav"
        ) { entry ->
            if (entry == null) {
                DirectorioHubScreen(
                    viewModel = viewModel,
                    onOpenDrawer = onOpenDrawer,
                    onCardClick = { selectedEntry = it },
                    listState = listState,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent
                )
            } else {
                DirectorioDetalleScreen(
                    entrada = entry,
                    hayInternet = viewModel.hayInternet,
                    onBack = { selectedEntry = null },
                    onNavigateToChat = onNavigateToChat,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent
                )
            }
        }
    }
}

// ── Hub Screen ────────────────────────────────────────

@Composable
private fun DirectorioHubScreen(
    viewModel: DirectorioViewModel,
    onOpenDrawer: () -> Unit,
    onCardClick: (EntradaDirectorio) -> Unit,
    listState: LazyListState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        GuiaTopBar(
            title = "Directorio Profesional",
            onBack = onOpenDrawer,
            isMenuButton = true,
            showBertoBadge = false
        )

        HeroSideBerto(
            mascot = R.drawable.berto_saludando,
            title = "Aquí puedes pedir ayuda profesional.",
            subtitle = "Todos los centros son gratuitos o de bajo costo."
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .offset(y = (-24).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.background)
        ) {
            OutlinedTextField(
                value = viewModel.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                placeholder = {
                    Text("Buscar por nombre o ciudad...", color = TealMedium.copy(alpha = 0.7f), fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = TealMedium,
                        modifier = Modifier.size(20.dp)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = TealLight,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                item {
                    FiltroChip(label = "Todos", isActive = viewModel.filtroActivo == null) {
                        viewModel.onFiltroChange(null)
                    }
                }
                items(TipoDirectorio.entries) { tipo ->
                    FiltroChip(label = tipo.label, isActive = viewModel.filtroActivo == tipo) {
                        viewModel.onFiltroChange(tipo)
                    }
                }
            }

            if (!viewModel.hayInternet) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(TealLightest)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_wifi_off),
                        contentDescription = null,
                        tint = TealDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Text("Cómo llegar requiere conexión a internet", fontSize = 12.sp, color = TealDark, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(12.dp))
            }

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.resultados, key = { it.id }) { entrada ->
                    DirectorioCard(
                        entrada = entrada,
                        hayInternet = viewModel.hayInternet,
                        onClick = { onCardClick(entrada) },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            }
        }
    }
}

// ── Filtro Chip ───────────────────────────────────────

@Composable
private fun FiltroChip(label: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(if (isActive) TealDark else Color.Transparent)
            .border(1.5.dp, if (isActive) TealDark else TealLight, RoundedCornerShape(99.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) Color.White else TealDark
        )
    }
}

// ── Card ──────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DirectorioCard(
    entrada: EntradaDirectorio,
    hayInternet: Boolean,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val context = LocalContext.current

    with(sharedTransitionScope) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "card_${entrada.id}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    enter = fadeIn(tween(350)),
                    exit = fadeOut(tween(250)),
                    clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(16.dp))
                )
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(0.5.dp, TealLight, RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            // Badges
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BadgeTipo(
                    tipo = entrada.tipo,
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = "badge_${entrada.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
                if (entrada.verificado) {
                    Spacer(Modifier.width(8.dp))
                    BadgeVerificado()
                }
                Spacer(Modifier.weight(1f))
                if (entrada.lat != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_map_pin), null, tint = TealMedium, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Abrir mapa", fontSize = 11.sp, color = TealMedium)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            if (entrada.tipo == TipoDirectorio.PSICOLOGO) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(TealLightest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painterResource(R.drawable.ic_user), null, tint = TealPrimary, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = entrada.nombre,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TealDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(key = "nombre_${entrada.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        )
                        entrada.especialidad?.let { Text(it, fontSize = 12.sp, color = TealPrimary) }
                    }
                }
                if (entrada.tipoCita != null || entrada.tipoCosto != null) {
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        entrada.tipoCita?.let { cita ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(painterResource(R.drawable.ic_clock), null, tint = TealMedium, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(cita, fontSize = 11.sp, color = TealMedium)
                            }
                        }
                        entrada.tipoCosto?.let { costo ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(painterResource(R.drawable.ic_tag), null, tint = TealMedium, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(costo, fontSize = 11.sp, color = TealMedium)
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = entrada.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TealDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = "nombre_${entrada.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
                Spacer(Modifier.height(6.dp))
                entrada.direccion?.let { dir ->
                    InfoRow(R.drawable.ic_map_pin, dir)
                    Spacer(Modifier.height(4.dp))
                }
                entrada.horario?.let { hora -> InfoRow(R.drawable.ic_clock, hora) }
                if (entrada.tags.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        entrada.tags.forEach { TagPill(it) }
                    }
                }
            }

            if (entrada.tipo == TipoDirectorio.LINEA) {
                Spacer(Modifier.height(8.dp))
                Text(entrada.telefono, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TealDark)
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = TealLight, thickness = 0.5.dp)
            Spacer(Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(99.dp))
                        .background(TealDark)
                        .clickable { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${entrada.telefono}"))) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(painterResource(R.drawable.ic_phone), null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Llamar", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                val canNavigate = entrada.lat != null && hayInternet
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(99.dp))
                        .border(1.5.dp, if (canNavigate) TealDark else TealLight, RoundedCornerShape(99.dp))
                        .then(if (canNavigate) Modifier.clickable {
                            val uri = Uri.parse("google.navigation:q=${entrada.lat},${entrada.lng}")
                            val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${entrada.lat},${entrada.lng}")))
                            }
                        } else Modifier)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(painterResource(R.drawable.ic_map_pin), null, tint = if (canNavigate) TealDark else TealLight, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cómo llegar", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (canNavigate) TealDark else TealLight)
                    }
                }
            }
        }
    }
}

// ── Detail Screen ─────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DirectorioDetalleScreen(
    entrada: EntradaDirectorio,
    hayInternet: Boolean,
    onBack: () -> Unit,
    onNavigateToChat: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val context = LocalContext.current

    with(sharedTransitionScope) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "card_${entrada.id}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    enter = fadeIn(tween(350)),
                    exit = fadeOut(tween(250)),
                    clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(0.dp))
                )
                .background(MaterialTheme.colorScheme.primary)
        ) {
            GuiaTopBar(
                title = entrada.nombre,
                onBack = onBack,
                isMenuButton = false,
                showBertoBadge = false
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 44.dp)
            ) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        BadgeTipo(
                            tipo = entrada.tipo,
                            modifier = Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(key = "badge_${entrada.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        )
                        if (entrada.verificado) BadgeVerificado()
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = entrada.nombre,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.sharedElement(
                            sharedContentState = rememberSharedContentState(key = "nombre_${entrada.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = entrada.descripcion,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .offset(y = (-24).dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        InfoCard {
                            if (entrada.tipo == TipoDirectorio.PSICOLOGO) {
                                entrada.especialidad?.let { esp ->
                                    InfoRow(R.drawable.ic_user, esp)
                                    Spacer(Modifier.height(10.dp))
                                }
                                entrada.tipoCita?.let { cita ->
                                    InfoRow(R.drawable.ic_clock, cita)
                                    Spacer(Modifier.height(10.dp))
                                }
                                entrada.tipoCosto?.let { costo ->
                                    InfoRow(R.drawable.ic_tag, costo)
                                    Spacer(Modifier.height(10.dp))
                                }
                            }
                            InfoRow(R.drawable.ic_phone, entrada.telefono)
                            entrada.horario?.let { hora ->
                                Spacer(Modifier.height(10.dp))
                                InfoRow(R.drawable.ic_clock, hora)
                            }
                            entrada.direccion?.let { dir ->
                                Spacer(Modifier.height(10.dp))
                                InfoRow(R.drawable.ic_map_pin, dir)
                            }
                            if (entrada.tags.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    entrada.tags.forEach { TagPill(it) }
                                }
                            }
                        }
                    }

                    if (entrada.lat != null && entrada.lng != null) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(TealLightest)
                                    .clickable {
                                        val uri = Uri.parse("google.navigation:q=${entrada.lat},${entrada.lng}")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                            .apply { setPackage("com.google.android.apps.maps") }
                                        if (intent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(intent)
                                        } else {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("https://maps.google.com/?q=${entrada.lat},${entrada.lng}"))
                                            )
                                        }
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(TealPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painterResource(R.drawable.ic_map_pin),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Cómo llegar", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TealDark)
                                    Text("Abrir en Google Maps", fontSize = 12.sp, color = TealMedium)
                                }
                                Icon(
                                    painterResource(R.drawable.ic_share),
                                    contentDescription = null,
                                    tint = TealMedium,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(99.dp))
                                .background(TealDark)
                                .clickable { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${entrada.telefono}"))) }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(painterResource(R.drawable.ic_phone), null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Llamar ahora", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(99.dp))
                                .border(1.5.dp, TealDark, RoundedCornerShape(99.dp))
                                .clickable(onClick = onNavigateToChat)
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Image(painterResource(R.drawable.berto_saludando), null, modifier = Modifier.size(24.dp), contentScale = ContentScale.Fit)
                                Spacer(Modifier.width(8.dp))
                                Text("Hablar con Berto", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TealDark)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Small components ──────────────────────────────────

@Composable
private fun BadgeTipo(tipo: TipoDirectorio, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(99.dp))
            .background(fondoBadgeTipo(tipo))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(tipo.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorBadgeTipo(tipo))
    }
}

@Composable
private fun BadgeVerificado() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(TealLightest)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_check), null, tint = TealPrimary, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text("Verificado", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
        }
    }
}


@Composable
private fun InfoRow(icon: Int, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(painterResource(icon), null, tint = TealMedium, modifier = Modifier.size(16.dp).padding(top = 1.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, color = TealDark, lineHeight = 18.sp)
    }
}

@Composable
private fun TagPill(text: String) {
    Box(
        modifier = Modifier
            .padding(bottom = 6.dp)
            .clip(RoundedCornerShape(99.dp))
            .border(1.dp, TealLight, RoundedCornerShape(99.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text, fontSize = 12.sp, color = TealDark, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, TealLight, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        content()
    }
}
