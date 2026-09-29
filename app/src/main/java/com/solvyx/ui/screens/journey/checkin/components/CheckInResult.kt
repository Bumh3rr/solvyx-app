package com.solvyx.ui.screens.journey.checkin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.AnimatedCountText
import com.solvyx.ui.components.common.ConfettiBurst
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxOutlinedButton
import com.solvyx.ui.components.common.diasLabel
import com.solvyx.ui.screens.journey.checkin.resultReaction
import com.solvyx.ui.theme.StreakFlame
import com.solvyx.ui.theme.TealDark

private val ResultBertoSize = 180.dp
private const val StreakTintAlpha = 0.14f
private const val GoalTintAlpha = 0.1f

/**
 * Closing screen after saving. A clean day celebrates (confetti + streak counting up); a day with
 * use gets comfort and a direct way to first aid. [streak] is null when it couldn't be updated
 * yet, and [pendingSync] means the entry is saved on the phone and will upload when online.
 */
@Composable
fun CheckInResult(
    used: Boolean,
    streak: Int?,
    pendingSync: Boolean,
    onDone: () -> Unit,
    onOpenFirstAid: () -> Unit,
    modifier: Modifier = Modifier,
    // "Avanzaste en tu meta" and which one; null when the day added to no goal (nothing is said).
    advancedTitle: String? = null,
    advancedDetail: String? = null
) {
    val reaction = resultReaction(used)
    Box(modifier = modifier.fillMaxSize()) {
        if (!used) ConfettiBurst(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BertoStage(reaction = reaction, lookX = null, bertoSize = ResultBertoSize)
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (used) "Registro guardado" else "¡Día registrado!",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark,
                textAlign = TextAlign.Center
            )
            if (!used && streak != null) {
                Spacer(Modifier.height(12.dp))
                StreakBadge(streak)
            }
            if (advancedTitle != null) {
                Spacer(Modifier.height(12.dp))
                GoalAdvancedBadge(title = advancedTitle, detail = advancedDetail)
            }
            if (pendingSync) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Se guardó en tu teléfono y se sincronizará cuando tengas internet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(28.dp))
            SolvyxButton(text = "Listo", onClick = onDone, modifier = Modifier.fillMaxWidth())
            if (used) {
                Spacer(Modifier.height(10.dp))
                SolvyxOutlinedButton(
                    text = "Ver primeros auxilios",
                    onClick = onOpenFirstAid,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun GoalAdvancedBadge(title: String, detail: String?) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary.copy(alpha = GoalTintAlpha), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check_circle),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TealDark
            )
            detail?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StreakBadge(streak: Int) {
    Row(
        modifier = Modifier
            .background(StreakFlame.copy(alpha = StreakTintAlpha), RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_flame),
            contentDescription = null,
            tint = StreakFlame,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        AnimatedCountText(
            value = streak,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = TealDark
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "${diasLabel(streak)} de racha",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TealDark
        )
    }
}
