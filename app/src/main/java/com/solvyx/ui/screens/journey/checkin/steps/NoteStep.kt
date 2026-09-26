package com.solvyx.ui.screens.journey.checkin.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.solvyx.ui.screens.journey.checkin.NOTE_MAX_LENGTH
import com.solvyx.ui.screens.journey.checkin.components.CheckInTextField
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

/** Quick ideas per mood, so writing starts with a tap instead of a blank box. */
private val NoteIdeas = mapOf(
    "triste" to listOf("Sentí soledad", "Tuve un día pesado", "Extraño a alguien", "No dormí bien"),
    "ansioso" to listOf("Mucha presión", "La escuela", "No pude relajarme", "Me preocupa algo"),
    "neutral" to listOf("Día normal", "Poco que contar", "Con cansancio", "Con calma"),
    "bien" to listOf("Dormí bien", "Vi a mis amigos", "Hice ejercicio", "Logré algo"),
    "euforico" to listOf("Gran noticia", "Me siento con energía", "Celebré algo", "Día increíble")
)
private val DefaultIdeas = listOf("La escuela", "Mi familia", "Mis amigos", "Dormí bien")

/** Optional note. Berto follows the cursor while the user types (see [CheckInTextField]). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteStep(
    mood: String?,
    note: String,
    onNoteChange: (String) -> Unit,
    onIdeaTap: (String) -> Unit,
    onCaretMoved: (Float?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        CheckInTextField(
            value = note,
            onValueChange = onNoteChange,
            placeholder = "Escribe cómo estuvo tu día…",
            maxLength = NOTE_MAX_LENGTH,
            onCaretMoved = onCaretMoved
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Ideas rápidas",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (NoteIdeas[mood] ?: DefaultIdeas).forEach { idea ->
                AssistChip(
                    onClick = { onIdeaTap(idea) },
                    label = { Text(idea, style = MaterialTheme.typography.bodySmall) },
                    shape = RoundedCornerShape(50),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceDim,
                        labelColor = TealDark
                    ),
                    border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = TealLight)
                )
            }
        }
    }
}
