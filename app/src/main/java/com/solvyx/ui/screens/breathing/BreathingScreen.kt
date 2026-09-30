package com.solvyx.ui.screens.breathing

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.solvyx.ui.screens.chatbot.BreathingSession

/**
 * "Respira conmigo" as its own screen (e.g. from Mi plan). Reuses the chat's [BreathingSession]
 * so both places guide the same rhythm with the same voice.
 */
@Composable
fun BreathingScreen(
    onFinish: () -> Unit,
    viewModel: BreathingViewModel = hiltViewModel()
) {
    BreathingSession(
        onPhase = viewModel::onPhase,
        onClose = {
            viewModel.stop()
            onFinish()
        }
    )
}
