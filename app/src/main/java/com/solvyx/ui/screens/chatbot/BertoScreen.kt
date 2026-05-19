package com.solvyx.ui.screens.chatbot

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.R
import com.solvyx.ui.components.dialog.SosConfirmationDialog
import com.solvyx.ui.components.common.SolvyxBackButton
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.theme.BertoVisorCalm
import com.solvyx.ui.theme.BertoVisorCelebr
import com.solvyx.ui.theme.BertoVisorCrisis
import com.solvyx.ui.theme.BertoVisorWorried
import com.solvyx.ui.theme.CrisisRed
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLightest
import com.solvyx.ui.theme.TealPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BertoScreen(
    onBack: () -> Unit,
    onNavigateToSos: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val listState = rememberLazyListState()
    val messages = viewModel.messages
    val context = LocalContext.current
    var showBertoCapabilities by remember { mutableStateOf(false) }

    // ── Reconocimiento de voz ──────────────────────────
    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!text.isNullOrEmpty()) viewModel.onInputChange(text)
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            speechLauncher.launch(buildSpeechIntent())
        }
    }

    // Scroll index: sentinel lives at messages.size, past all items.
    // Re-scroll after chip animation delay so FlowRow height is already laid out.
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
            if (messages.lastOrNull()?.quickReplies?.isNotEmpty() == true) {
                delay(450)
                listState.animateScrollToItem(messages.size)
            }
        }
    }

    // ── Diálogos / sheets ─────────────────────────────
    if (viewModel.showSosDialog) {
        SosConfirmationDialog(
            onConfirm = {
                viewModel.toggleSosDialog()
                onNavigateToSos()
            },
            onDismiss = { viewModel.toggleSosDialog() }
        )
    }
    if (showBertoCapabilities) {
        BertoCapabilitiesSheet(onDismiss = { showBertoCapabilities = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {

        ChatTopBar(
            bertoState        = viewModel.currentBertoState,
            isTtsMuted        = viewModel.isTtsMuted,
            isSpeaking        = viewModel.isSpeaking,
            onToggleMute      = { viewModel.toggleMute() },
            onBack            = onBack,
            onClearMessages   = { viewModel.clearMessages() },
            onShowCapabilities = { showBertoCapabilities = true },
            onSosClick        = { viewModel.toggleSosDialog() }
        )

        AnimatedVisibility(
            visible = viewModel.stateTransition != null,
            enter = slideInVertically { -it } + fadeIn(tween(250)),
            exit  = slideOutVertically { -it } + fadeOut(tween(250))
        ) {
            viewModel.stateTransition?.let { BertoStateTransitionBanner(it) }
        }

        AnimatedVisibility(
            visible = viewModel.showBertoPeek,
            enter = expandVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) +
                    fadeIn(tween(300)),
            exit = shrinkVertically(tween(250)) + fadeOut(tween(200))
        ) {
            BertoPeekZone(
                bertoState = viewModel.currentBertoState,
                isTyping = viewModel.isBertoTyping
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = messages, key = { it.id }) { message ->
                val alpha = remember { Animatable(0f) }
                val slide = remember {
                    Animatable(if (message.isFromBerto) -20f else 20f)
                }
                LaunchedEffect(message.id) {
                    launch { alpha.animateTo(1f, tween(300)) }
                    launch {
                        slide.animateTo(
                            0f,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alpha.value)
                        .offset(x = slide.value.dp)
                ) {
                    MessageBubble(message = message)
                    val isLastBertoMsg = message.isFromBerto &&
                            message.quickReplies.isNotEmpty() &&
                            message == messages.lastOrNull { it.isFromBerto }
                    if (isLastBertoMsg) {
                        QuickRepliesRow(
                            replies = message.quickReplies,
                            onReplySelected = { viewModel.sendMessage(it) }
                        )
                    }
                }
            }
            // Sentinel: scrolling to this index guarantees chips above are fully visible
            item(key = "bottom_sentinel") {
                Spacer(Modifier.height(1.dp))
            }
        }

        ChatInputBar(
            text = viewModel.inputText,
            onTextChange = viewModel::onInputChange,
            onSend = { viewModel.sendMessage() },
            onSosClick = { viewModel.toggleSosDialog() },
            onMicClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    speechLauncher.launch(buildSpeechIntent())
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            isSendEnabled = viewModel.inputText.isNotBlank()
        )
    }
}

private fun buildSpeechIntent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
    putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla con Berto...")
}

// ── Top bar ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    bertoState        : BertoState,
    isTtsMuted        : Boolean,
    isSpeaking        : Boolean,
    onToggleMute      : () -> Unit,
    onBack            : () -> Unit,
    onClearMessages   : () -> Unit,
    onShowCapabilities: () -> Unit,
    onSosClick        : () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val avatarBg = when (bertoState) {
        BertoState.TRANQUILO  -> BertoVisorCalm
        BertoState.PREOCUPADO -> BertoVisorWorried
        BertoState.CELEBRANDO -> BertoVisorCelebr
        BertoState.CRISIS     -> BertoVisorCrisis
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SolvyxBackButton(onClick = onBack)

        Spacer(Modifier.size(8.dp))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(avatarBg)
                .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = bertoState,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "BertoAvatar"
            ) { state ->
                Image(
                    painter = painterResource(bertoPainterRes(state)),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(Modifier.size(10.dp))

        Column(Modifier.weight(1f)) {
            Text(
                "Berto",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            val inf = rememberInfiniteTransition(label = "OnlinePulse")
            val pulseAlpha by inf.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                label = "PulseAlpha"
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50).copy(alpha = pulseAlpha))
                )
                Spacer(Modifier.size(4.dp))
                AnimatedContent(
                    targetState = bertoState,
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInVertically { it / 2 }) togetherWith
                        (fadeOut(tween(200)) + slideOutVertically { -it / 2 })
                    },
                    label = "BertoStatus"
                ) { state ->
                    Text(
                        text = when (state) {
                            BertoState.TRANQUILO  -> "En línea · Privado"
                            BertoState.PREOCUPADO -> "Aquí para ti"
                            BertoState.CELEBRANDO -> "¡Celebrando contigo!"
                            BertoState.CRISIS     -> "Modo de apoyo activo"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.80f)
                    )
                }
            }
        }

        // Badge CRISIS animado
        AnimatedVisibility(
            visible = bertoState == BertoState.CRISIS,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) +
                    fadeIn(tween(300)),
            exit = scaleOut(tween(200)) + fadeOut(tween(200))
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(CrisisRed)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Spacer(Modifier.size(5.dp))
                    Text(
                        "CRISIS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }

        // Botón de voz + ecualizador animado cuando habla
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedVisibility(visible = isSpeaking && !isTtsMuted) {
                SoundWaveIndicator(modifier = Modifier.padding(end = 2.dp))
            }
            IconButton(onClick = onToggleMute) {
                Icon(
                    painter           = painterResource(
                        if (isTtsMuted) R.drawable.ic_volume_off else R.drawable.ic_volume_on
                    ),
                    contentDescription = if (isTtsMuted) "Activar voz" else "Silenciar voz",
                    tint              = if (isSpeaking && !isTtsMuted) Color.White
                                        else Color.White.copy(alpha = 0.65f),
                    modifier          = Modifier.size(20.dp)
                )
            }
        }

        // Menú tres puntos
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vertical),
                    contentDescription = "Más opciones",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Limpiar conversación",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.ic_trash),
                            null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = { onClearMessages(); showMenu = false }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            "¿Qué puede hacer Berto?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.ic_info_circle),
                            null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = { onShowCapabilities(); showMenu = false }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            "Activar modo crisis",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CrisisRed
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.ic_alert_triangle),
                            null,
                            tint = CrisisRed,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = { onSosClick(); showMenu = false }
                )
            }
        }
    }
}

// ── Berto peek mientras escribe ─────────────────────────
@Composable
private fun BertoPeekZone(bertoState: BertoState, isTyping: Boolean) {
    val bgColor = when (bertoState) {
        BertoState.CRISIS     -> BertoVisorCrisis
        BertoState.PREOCUPADO -> BertoVisorWorried
        BertoState.CELEBRANDO -> TealLightest
        BertoState.TRANQUILO  -> TealLightest
    }

    val inf = rememberInfiniteTransition(label = "PeekFloat")
    val floatAnim by inf.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "PeekFloat"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedContent(
            targetState = Pair(bertoState, isTyping),
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "BertoPeekImage"
        ) { (state, typing) ->
            Image(
                painter = painterResource(
                    if (typing) R.drawable.berto_pregunta
                    else bertoPainterRes(state)
                ),
                contentDescription = "Berto",
                modifier     = Modifier
                    .size(52.dp)
                    .offset(y = floatAnim.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(Modifier.size(12.dp))
        Column {
            Text(
                if (isTyping) "Berto está escribiendo..." else "Berto",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TealDark
            )
            if (isTyping) TypingIndicator()
        }
    }
}

// ── Tres puntos pulsantes ───────────────────────────────
@Composable
private fun TypingIndicator() {
    val inf = rememberInfiniteTransition(label = "Typing")
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        repeat(3) { idx ->
            val dotAlpha by inf.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = idx * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "Dot$idx"
            )
            val dotSize by inf.animateFloat(
                initialValue = 6f,
                targetValue = 8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = idx * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "DotSize$idx"
            )
            Box(
                Modifier
                    .size(dotSize.dp)
                    .clip(CircleShape)
                    .background(TealPrimary.copy(alpha = dotAlpha))
            )
        }
    }
}

// ── Burbuja de mensaje ──────────────────────────────────
@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = !message.isFromBerto
    val bubbleShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isUser) 18.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 18.dp
    )
    val bertoBorderColor = stateAccentColor(message.bertoState)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(
                    if (isUser) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                )
                .then(
                    if (!isUser) Modifier.border(0.8.dp, bertoBorderColor, bubbleShape)
                    else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUser) Color.White.copy(alpha = 0.65f)
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

// ── Chips de respuesta rápida ───────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickRepliesRow(
    replies: List<String>,
    onReplySelected: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            replies.forEachIndexed { idx, reply ->
                var chipVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(idx * 100L)
                    chipVisible = true
                }
                AnimatedVisibility(
                    visible = chipVisible,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) +
                            fadeIn(tween(200))
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50.dp))
                            .clickable { onReplySelected(reply) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            reply,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// ── Barra de entrada ─────────────────────────────────────
@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onSosClick: () -> Unit,
    onMicClick: () -> Unit,
    isSendEnabled: Boolean
) {
    val sendScale by animateFloatAsState(
        targetValue = if (isSendEnabled) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "SendScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón SOS
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CrisisRed.copy(alpha = 0.12f))
                .clickable { onSosClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_alert_triangle),
                contentDescription = "SOS",
                tint = CrisisRed,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.size(8.dp))

        // Campo de texto + mic
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.background)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .padding(end = 36.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                maxLines = 4,
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            "Escribe a Berto...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            )
            // Botón micrófono
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = (-4).dp)
                    .clip(CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onMicClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mic),
                    contentDescription = "Voz",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.size(8.dp))

        // Botón enviar
        Box(
            modifier = Modifier
                .size(44.dp)
                .scale(sendScale)
                .clip(CircleShape)
                .background(
                    if (isSendEnabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.primaryContainer
                )
                .clickable(enabled = isSendEnabled) { onSend() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_send),
                contentDescription = "Enviar",
                tint = if (isSendEnabled) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Sound wave indicator ─────────────────────────────────

@Composable
private fun SoundWaveIndicator(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "SoundWave")
    Row(
        modifier              = modifier,
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val heights = listOf(6f, 13f, 8f)   // alturas base distintas → look orgánico
        heights.forEachIndexed { i, base ->
            val h by inf.animateFloat(
                initialValue = base,
                targetValue  = base + 7f,
                animationSpec = infiniteRepeatable(
                    animation  = tween(300 + i * 90, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "Bar$i"
            )
            Box(
                Modifier
                    .width(2.5.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White)
            )
        }
    }
}

// ── State helpers ────────────────────────────────────────

private fun bertoPainterRes(state: BertoState): Int = when (state) {
    BertoState.TRANQUILO  -> R.drawable.berto_saludando
    BertoState.PREOCUPADO -> R.drawable.berto_preocupado
    BertoState.CELEBRANDO -> R.drawable.berto_euforico
    BertoState.CRISIS     -> R.drawable.berto_preocupado
}

@Composable
private fun stateAccentColor(state: BertoState): Color = when (state) {
    BertoState.TRANQUILO  -> MaterialTheme.colorScheme.outline
    BertoState.PREOCUPADO -> Color(0xFFFF8F00)
    BertoState.CELEBRANDO -> TealPrimary
    BertoState.CRISIS     -> CrisisRed
}

// ── State transition banner ──────────────────────────────

@Composable
private fun BertoStateTransitionBanner(state: BertoState) {
    data class BannerCfg(val iconRes: Int, val label: String, val bg: Color, val tint: Color)
    val cfg = when (state) {
        BertoState.TRANQUILO  -> BannerCfg(R.drawable.ic_check_circle, "Berto está aquí para ti",       TealLightest,                 TealPrimary)
        BertoState.PREOCUPADO -> BannerCfg(R.drawable.ic_heart_pulse,  "Berto se preocupa por ti",       Color(0xFFFFF3E0),             Color(0xFFBF360C))
        BertoState.CELEBRANDO -> BannerCfg(R.drawable.ic_trophy,       "¡Berto celebra este logro! 🎉",  TealLightest,                 TealPrimary)
        BertoState.CRISIS     -> BannerCfg(R.drawable.ic_alert_triangle,"Berto activó el apoyo de crisis",CrisisRed.copy(alpha = 0.10f), CrisisRed)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(cfg.bg)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter           = painterResource(cfg.iconRes),
            contentDescription = null,
            tint              = cfg.tint,
            modifier          = Modifier.size(15.dp)
        )
        Text(
            text  = cfg.label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = cfg.tint
        )
    }
}

// ── Bottom Sheet: Capacidades de Berto ──────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BertoCapabilitiesSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.berto_mira_moriposa),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "¿Qué puede hacer Berto?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            val capacidades = listOf(
                Triple(R.drawable.ic_chat, "Escucharte",
                    "Puedes contarle cómo te sientes en cualquier momento"),
                Triple(R.drawable.ic_heart_pulse, "Técnicas de calma",
                    "Respiración, grounding y manejo del craving"),
                Triple(R.drawable.ic_alert_triangle, "Detectar crisis",
                    "Si detecta que estás en riesgo, activa el modo de apoyo"),
                Triple(R.drawable.ic_shield, "Privado",
                    "Todo lo que le dices se queda solo en tu teléfono")
            )

            capacidades.forEach { (iconRes, titulo, descripcion) ->
                val isCrisis = titulo == "Detectar crisis"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isCrisis) CrisisRed.copy(alpha = 0.10f)
                                else MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            tint = if (isCrisis) CrisisRed else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp)
                    ) {
                        Text(
                            titulo,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isCrisis) CrisisRed
                            else MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            SolvyxButton(
                text = "Entendido",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}