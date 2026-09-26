package com.solvyx.ui.screens.firstaid.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.solvyx.ui.components.berto.BertoSpeechRow
import com.solvyx.ui.screens.firstaid.model.GuideHero

/** Opening of every guide: Berto talking to the user in first person, before any instruction. */
@Composable
fun BertoSpeechHero(hero: GuideHero, modifier: Modifier = Modifier) {
    BertoSpeechRow(message = hero.message, supporting = hero.supporting, modifier = modifier) {
        FirstAidBerto(mood = hero.mood, modifier = Modifier.fillMaxSize())
    }
}
