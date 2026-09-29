package com.solvyx.ui.screens.journey.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.moodOption
import com.solvyx.ui.theme.StreakFlame

/**
 * Day check-in at the top of the Progress tab. Replaces the old "Hoy" tab: logging is a
 * daily action, not a permanent view. Two states:
 *  - pending: card that invites the user to log (greeting based on streak) and launches the wizard.
 *  - logged: thin row that confirms and offers to edit.
 */
@Composable
fun CheckInCard(
    todayEntry: JournalEntry?,
    streak: Int,
    onRegister: () -> Unit,
    onEdit: () -> Unit,
    onOpenStreak: () -> Unit = {},
) {
    if (todayEntry != null) {
        LoggedRow(entry = todayEntry, streak = streak, onEdit = onEdit, onOpenStreak = onOpenStreak)
    } else {
        PendingCard(streak = streak, onRegister = onRegister)
    }
}

@Composable
private fun PendingCard(streak: Int, onRegister: () -> Unit, modifier: Modifier = Modifier) {
    val greeting = if (streak > 0) "Vas $streak días. ¿Cómo estuvo hoy?" else "¿Cómo estuvo tu día?"
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BertoPoseAnimation(
                    pose = BertoPose.RIGHT,
                    riveFileRes = R.raw.berto_poses,
                            modifier = Modifier.size(52.dp),
                    fallback = R.drawable.berto_dedo_der
                )
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tu check-in de hoy",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.size(2.dp))
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            SolvyxButton(
                text = "Registrar mi día",
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_heart),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun LoggedRow(
    entry: JournalEntry,
    streak: Int,
    onEdit: () -> Unit,
    onOpenStreak: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mood = moodOption(entry.mood)
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(mood.icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_circle),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.size(5.dp))
                    Text(
                        text = "Registrado hoy",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = buildString {
                        append(mood.label)
                        append(" · ")
                        append(if (entry.consumed == true) "Con consumo" else "Sin consumo")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (streak > 0) StreakChip(streak = streak, onClick = onOpenStreak)
            }
            Text(
                text = "Editar",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onEdit() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

/** "Racha N días ›": the streak lives in Logros (the trail); this is the shortcut to it. */
@Composable
private fun StreakChip(streak: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(top = 6.dp)
            .clip(RoundedCornerShape(50))
            .background(StreakFlame.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(R.drawable.ic_flame), null, tint = StreakFlame, modifier = Modifier.size(14.dp))
        Spacer(Modifier.size(4.dp))
        Text(
            if (streak == 1) "Racha 1 día" else "Racha $streak días",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = StreakFlame
        )
        Icon(painterResource(R.drawable.ic_chevron_right), null, tint = StreakFlame, modifier = Modifier.size(14.dp))
    }
}
