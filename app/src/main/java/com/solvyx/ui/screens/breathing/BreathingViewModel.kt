package com.solvyx.ui.screens.breathing

import android.content.Context
import androidx.lifecycle.ViewModel
import com.solvyx.ui.screens.chatbot.BertoVoice
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Berto's voice for the standalone breathing screen: he says each phase out loud, as in the chat. */
@HiltViewModel
class BreathingViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    private val voice = BertoVoice(context)

    fun onPhase(phaseName: String) = voice.speak(phaseName)

    fun stop() = voice.stop()

    override fun onCleared() {
        voice.shutdown()
        super.onCleared()
    }
}
