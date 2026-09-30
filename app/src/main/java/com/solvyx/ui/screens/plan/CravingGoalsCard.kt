package com.solvyx.ui.screens.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.solvyx.R
import com.solvyx.backend.repository.GoalRepository
import com.solvyx.backend.repository.JournalRepository
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.theme.TealDark
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

private const val STOP_TIMEOUT_MS = 5_000L

/** The days-without-use goals to remind of in "Ganas muy fuertes", from the local cache. */
@HiltViewModel
class CravingGoalsViewModel @Inject constructor(
    goalRepository: GoalRepository,
    journalRepository: JournalRepository
) : ViewModel() {

    val goals: StateFlow<List<GoalCardUi>> =
        combine(goalRepository.observeGoals(), journalRepository.observeAll()) { goals, journal ->
            cravingGoals(goals, journal, LocalDate.now())
        }
            .catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())
}

/**
 * "Tus metas" inside the craving guide: how far the user already got, as motivation and without
 * pressure. Not tappable (it must not pull anyone out of the guide) and absent when there is no
 * such goal: no "you have no goals" message in a hard moment.
 */
@Composable
fun CravingGoalsCard(
    modifier: Modifier = Modifier,
    viewModel: CravingGoalsViewModel = hiltViewModel()
) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    if (goals.isEmpty()) return

    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconCapsule(R.drawable.ic_target)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Tus metas",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = TealDark
                    )
                    Text(
                        "Esta ola pasa. Tu avance se queda.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            goals.forEach { card ->
                Column {
                    Text(
                        card.goal.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    GoalProgressBar(card.fraction)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        card.progressLabel,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                "Pase lo que pase hoy, tu avance no se borra.",
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
