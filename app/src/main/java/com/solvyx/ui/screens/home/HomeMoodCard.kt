package com.solvyx.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.theme.MoodAnsioso
import com.solvyx.ui.theme.MoodBien
import com.solvyx.ui.theme.MoodEuforico
import com.solvyx.ui.theme.MoodNeutral
import com.solvyx.ui.theme.MoodTriste

private data class MoodOption(val id: String, val label: String, val icon: Int, val color: Color)

private val moodOptions = listOf(
    MoodOption("triste", "Triste", R.drawable.ic_face_sad, MoodTriste),
    MoodOption("ansioso", "Ansioso", R.drawable.ic_face_anxious, MoodAnsioso),
    MoodOption("neutral", "Neutral", R.drawable.ic_face_neutral, MoodNeutral),
    MoodOption("bien", "Bien", R.drawable.ic_face_happy, MoodBien),
    MoodOption("euforico", "Eufórico", R.drawable.ic_face_euphoric, MoodEuforico)
)

@Composable
fun HomeMoodCard(
    moodToday: String?,
    onMoodSelected: (String) -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToEjercicio: () -> Unit,
    onNavigateToRegistro: () -> Unit,
    onNavigateToRedApoyo: () -> Unit,
    modifier: Modifier = Modifier
) {
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿Cómo te sientes hoy?",
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (moodToday != null) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Registrado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                moodOptions.forEach { option ->
                    val selected = moodToday == option.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onMoodSelected(option.id) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) option.color else option.color.copy(alpha = 0.15f)
                                )
                                .then(
                                    if (!selected) Modifier.border(
                                        1.5.dp,
                                        option.color.copy(alpha = 0.4f),
                                        CircleShape
                                    ) else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(option.icon),
                                contentDescription = option.label,
                                tint = if (selected) Color.White else option.color,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            option.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (selected) option.color else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = moodToday != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    EmocionSugerenciaCard(
                        mood = moodToday ?: "neutral",
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToEjercicio = onNavigateToEjercicio,
                        onNavigateToRegistro = onNavigateToRegistro,
                        onNavigateToRedApoyo = onNavigateToRedApoyo
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Tu registro se guarda de forma privada en tu cuenta.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EmocionSugerenciaCard(
    mood: String,
    onNavigateToChat: () -> Unit,
    onNavigateToEjercicio: () -> Unit,
    onNavigateToRegistro: () -> Unit,
    onNavigateToRedApoyo: () -> Unit
) {
    val iconRes = when (mood) {
        "bien", "neutral" -> R.drawable.ic_trending_up
        "ansioso"         -> R.drawable.ic_wind
        "euforico"        -> R.drawable.ic_people
        else              -> R.drawable.ic_chat
    }
    val mensaje = when (mood) {
        "bien"     -> "¡Qué bueno escuchar eso! Buen momento para registrar tu día."
        "neutral"  -> "Un día tranquilo también cuenta. Gracias por registrarlo."
        "ansioso"  -> "Prueba un ejercicio de respiración."
        "triste"   -> "Registrar cómo te sientes toma valentía. Berto puede escucharte."
        "euforico" -> "¡Qué energía! Comparte este momento con tu red de apoyo."
        else       -> "¡Qué energía! Aprovéchala con sabiduría."
    }
    val accion = when (mood) {
        "bien", "neutral" -> "Ir al registro"
        "ansioso"         -> "Respirar ahora"
        "euforico"        -> "Ver mi red"
        else              -> "Hablar con Berto"
    }
    val onAccion: () -> Unit = when (mood) {
        "bien", "neutral" -> onNavigateToRegistro
        "ansioso"         -> onNavigateToEjercicio
        "euforico"        -> onNavigateToRedApoyo
        else              -> onNavigateToChat
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
        Text(
            text = accion,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onAccion() }
        )
    }
}
