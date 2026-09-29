package com.solvyx.ui.screens.journey.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.navigation.SolvyxBottomNavClearance
import com.solvyx.ui.screens.journey.AchievementsUiState
import com.solvyx.ui.screens.journey.achievements.components.AchievementTrail
import com.solvyx.ui.screens.journey.achievements.components.DiaryBadgesGrid
import com.solvyx.ui.screens.journey.achievements.components.GoalMedalsGrid
import com.solvyx.ui.screens.journey.achievements.components.MedalDetailSheet
import com.solvyx.ui.screens.journey.achievements.components.MedalInfo
import com.solvyx.ui.screens.journey.achievements.components.TrophyHero
import com.solvyx.ui.screens.journey.achievements.tierIndexOf
import com.solvyx.ui.screens.journey.achievements.toGoalMedalInfo
import com.solvyx.ui.screens.journey.achievements.toMedalInfo
import com.solvyx.ui.screens.journey.achievements.trailMessage
import com.solvyx.ui.theme.TealDark

/**
 * Logros: a trophy hero, the streak achievements as a trail Berto walks along, the goal medals
 * and the diary badges. Any medal opens its detail. The "just unlocked" celebration lives in `JourneyScreen`,
 * so it shows up wherever the user is when a streak milestone is reached.
 */
@Composable
fun AchievementsTab(state: AchievementsUiState, modifier: Modifier = Modifier) {
    when (state) {
        AchievementsUiState.Loading -> CenteredMessage(
            image = R.drawable.berto_dedo_der,
            title = "Cargando tus logros…",
            modifier = modifier
        )
        AchievementsUiState.Empty -> CenteredMessage(
            image = R.drawable.berto_dedo_der,
            title = "Aún no hay logros por aquí",
            subtitle = "Registra tus días sin consumo para desbloquear tu primer logro.",
            modifier = modifier
        )
        is AchievementsUiState.Content -> AchievementsContent(state, modifier)
    }
}

@Composable
private fun AchievementsContent(state: AchievementsUiState.Content, modifier: Modifier) {
    var selected by remember { mutableStateOf<MedalInfo?>(null) }
    val milestones = state.achievements.map { it.threshold }.sorted()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = SolvyxBottomNavClearance)
    ) {
        TrophyHero(
            unlocked = state.unlockedCount,
            total = state.achievements.size,
            badgesUnlocked = state.badges.count { it.unlocked },
            badgesTotal = state.badges.size,
            message = trailMessage(state.currentStreak, milestones),
            goalMedalsUnlocked = state.goalMedals.count { it.unlocked },
            goalMedalsTotal = state.goalMedals.size
        )
        SectionTitle("Mi sendero", subtitle = "Cada medalla es una racha de días sin consumo")
        AchievementTrail(
            achievements = state.achievements,
            streak = state.currentStreak,
            onSelect = { achievement ->
                selected = achievement.toMedalInfo(tierIndexOf(achievement, state.achievements), state.currentStreak)
            }
        )
        if (state.goalMedals.isNotEmpty()) {
            SectionTitle("Mis metas", subtitle = "Cada medalla es una meta que cumpliste")
            GoalMedalsGrid(
                medals = state.goalMedals,
                completedGoals = state.completedGoals,
                onSelect = { medal ->
                    selected = medal.toGoalMedalInfo(tierIndexOf(medal, state.goalMedals), state.completedGoals)
                }
            )
        }
        if (state.badges.isNotEmpty()) {
            SectionTitle("Insignias del diario", subtitle = "Por escribir tus días y por tu honestidad")
            DiaryBadgesGrid(badges = state.badges, onSelect = { selected = it.toMedalInfo() })
        }
    }

    selected?.let { MedalDetailSheet(info = it, onDismiss = { selected = null }) }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(Modifier.padding(top = 28.dp, bottom = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = TealDark)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CenteredMessage(
    image: Int,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier.height(96.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        subtitle?.let {
            Spacer(Modifier.height(6.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
