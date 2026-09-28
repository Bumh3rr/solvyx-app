package com.solvyx.ui.screens.chatbot

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.ui.components.dialog.SosConfirmationDialog

private const val SPEECH_LOCALE = "es-MX"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BertoScreen(
    onBack: () -> Unit,
    onNavigateToSos: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    var showCapabilities by remember { mutableStateOf(false) }
    ResizeWindowForKeyboard()
    val onMicClick = rememberSpeechInput(onResult = viewModel::onInputChange)
    val isThinking = viewModel.isBertoTyping || viewModel.isWaitingForApi
    val inCrisis = viewModel.currentBertoState == BertoState.CRISIS

    if (viewModel.showSosDialog) {
        SosConfirmationDialog(
            onConfirm = {
                viewModel.toggleSosDialog()
                onNavigateToSos()
            },
            onDismiss = { viewModel.toggleSosDialog() }
        )
    }
    if (showCapabilities) {
        BertoCapabilitiesSheet(onDismiss = { showCapabilities = false })
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
        ) {
            ChatTopBar(
                chatMode = viewModel.chatMode,
                isTtsMuted = viewModel.isTtsMuted,
                isSpeaking = viewModel.isSpeaking,
                onToggleMute = viewModel::toggleMute,
                onBack = onBack,
                onBreathe = viewModel::openBreathing,
                onRequestHelp = viewModel::requestCrisisSupport,
                onShowCapabilities = { showCapabilities = true },
                onClearMessages = viewModel::clearMessages
            )

            BertoStage(
                expression = stageExpression(
                    state = viewModel.currentBertoState,
                    isThinking = isThinking,
                    isThinkingLong = viewModel.isApiSlow,
                    isUserTyping = viewModel.inputText.isNotBlank(),
                    isOffline = viewModel.chatMode == ChatMode.SIN_CONEXION,
                    isWelcome = viewModel.messages.size <= 1
                ),
                state = viewModel.currentBertoState,
                celebrationCount = viewModel.celebrationCount,
                collapsed = WindowInsets.isImeVisible
            )

            SlideDownVisibility(visible = inCrisis) {
                CrisisSupportBar(onAction = viewModel::onSupportAction, onFeelBetter = viewModel::leaveCrisisSupport)
            }

            // Does not block anything: the guided topics and SOS keep working while it is open.
            // Hidden during a crisis: the help actions need that room more than a privacy question.
            SlideDownVisibility(visible = viewModel.showAiConsentPrompt && !inCrisis) {
                AiConsentCard(onAccept = viewModel::acceptAiConsent, onPostpone = viewModel::postponeAiConsent)
            }

            ChatMessageList(
                messages = viewModel.messages,
                activeInteractiveId = viewModel.activeInteractiveMessageId,
                choicesEnabled = viewModel.canUseChoices,
                showTyping = isThinking,
                typingState = viewModel.currentBertoState,
                isThinkingLong = viewModel.isApiSlow,
                onQuickReply = viewModel::onQuickReplySelected,
                onTopicSelected = viewModel::onTopicSelected,
                onSubstanceSelected = viewModel::onSubstanceSelected,
                onSupportAction = viewModel::onSupportAction,
                modifier = Modifier.weight(1f)
            )

            ChatInputBar(
                text = viewModel.inputText,
                chatMode = viewModel.chatMode,
                inCrisis = inCrisis,
                onTextChange = viewModel::onInputChange,
                onSend = { viewModel.sendMessage() },
                onSosClick = viewModel::toggleSosDialog,
                onMicClick = onMicClick,
                isInputEnabled = !viewModel.isWaitingForApi
            )
        }

        AnimatedVisibility(
            visible = viewModel.isBreathingOpen,
            enter = fadeIn(tween(350)) + scaleIn(tween(350), initialScale = 0.96f),
            exit = fadeOut(tween(250)) + scaleOut(tween(250), targetScale = 0.96f)
        ) {
            BreathingSession(onPhase = viewModel::onBreathPhase, onClose = viewModel::closeBreathing)
        }
    }
}

/**
 * With edge-to-edge and no soft-input mode, Android pans the whole window when the keyboard opens and
 * `imePadding()` adds the keyboard height again: the top bar slides off-screen and a keyboard-sized
 * gap appears. The chat resizes instead, and restores the previous mode for the other screens.
 */
@Composable
private fun ResizeWindowForKeyboard() {
    val window = (LocalView.current.context as? Activity)?.window ?: return
    DisposableEffect(window) {
        val previousMode = window.attributes.softInputMode
        @Suppress("DEPRECATION") // Still the mode that hands keyboard insets to Compose on every API level.
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        onDispose { window.setSoftInputMode(previousMode) }
    }
}

@Composable
private fun SlideDownVisibility(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(tween(300)) + fadeIn(tween(300)),
        exit = shrinkVertically(tween(250)) + fadeOut(tween(200))
    ) { content() }
}

/** Mic button behavior: asks for the permission once, then opens the system speech recognizer. */
@Composable
private fun rememberSpeechInput(onResult: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.takeIf { it.isNotEmpty() }
                ?.let(onResult)
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) speechLauncher.launch(buildSpeechIntent())
    }
    return {
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (hasPermission) speechLauncher.launch(buildSpeechIntent())
        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
}

private fun buildSpeechIntent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, SPEECH_LOCALE)
    putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla con Berto...")
}
