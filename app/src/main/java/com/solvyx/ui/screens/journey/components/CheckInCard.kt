package com.solvyx.ui.screens.journey.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxCard

private val faceIcons = mapOf(
    "triste" to R.drawable.ic_face_sad, "ansioso" to R.drawable.ic_face_anxious,
    "neutral" to R.drawable.ic_face_neutral, "bien" to R.drawable.ic_face_happy,
    "euforico" to R.drawable.ic_face_euphoric
)
private val faceLabels = mapOf(
    "triste" to "Triste", "ansioso" to "Ansioso", "neutral" to "Neutral",
    "bien" to "Bien", "euforico" to "Eufórico"
)

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
) {
    if (todayEntry != null) {
        LoggedRow(entry = todayEntry, onEdit = onEdit)
    } else {
        PendingCard(streak = streak, onRegister = onRegister)
    }
}

@Composable
private fun PendingCard(streak: Int, onRegister: () -> Unit, modifier: Modifier = Modifier) {
    val greeting = if (streak > 0) "Vas $streak días. ¿Cómo estuvo hoy?" else "¿Cómo estuvo tu día?"
    SolvyxCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun LoggedRow(entry: JournalEntry, onEdit: () -> Unit, modifier: Modifier = Modifier) {
    val mood = entry.mood ?: "neutral"
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
                    painter = painterResource(faceIcons[mood] ?: R.drawable.ic_face_neutral),
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
                        append(faceLabels[mood] ?: "Neutral")
                        append(" · ")
                        append(if (entry.consumed == true) "Con consumo" else "Sin consumo")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
