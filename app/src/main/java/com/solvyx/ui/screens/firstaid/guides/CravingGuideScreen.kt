package com.solvyx.ui.screens.firstaid.guides

import androidx.compose.runtime.Composable
import com.solvyx.ui.screens.firstaid.components.ActionChecklistCard
import com.solvyx.ui.screens.firstaid.components.BertoSpeechHero
import com.solvyx.ui.screens.firstaid.components.GuideScaffold
import com.solvyx.ui.screens.firstaid.components.InfoSectionCard
import com.solvyx.ui.screens.firstaid.content.CravingGuideContent
import com.solvyx.ui.screens.firstaid.model.SosEmphasis
import com.solvyx.ui.screens.plan.CravingGoalsCard

/** "Craving muy intenso": riding out the wave with a timer is the main action. */
@Composable
fun CravingGuideScreen(
    onBack: () -> Unit,
    onSos: () -> Unit
) {
    GuideScaffold(
        title = "Ganas muy fuertes",
        onBack = onBack,
        onSos = onSos,
        sosEmphasis = SosEmphasis.OUTLINED
    ) {
        BertoSpeechHero(hero = CravingGuideContent.hero)
        CravingWaveTimerCard()
        // The user's days-without-use goals, as motivation; nothing shows without them.
        CravingGoalsCard()
        ActionChecklistCard(plan = CravingGuideContent.plan)
        InfoSectionCard(section = CravingGuideContent.whatIsCraving)
        InfoSectionCard(section = CravingGuideContent.saferUse)
    }
}
