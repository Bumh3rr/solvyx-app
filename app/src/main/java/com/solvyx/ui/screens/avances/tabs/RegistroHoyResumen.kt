package com.solvyx.ui.screens.avances.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.ui.components.common.SolvyxButton

@Composable
fun RegistroHoyResumen(
    entry: JournalEntry,
    onEditar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val faceIcons = mapOf(
        "triste" to R.drawable.ic_face_sad, "ansioso" to R.drawable.ic_face_anxious,
        "neutral" to R.drawable.ic_face_neutral, "bien" to R.drawable.ic_face_happy,
        "euforico" to R.drawable.ic_face_euphoric
    )
    val faceLabels = mapOf(
        "triste" to "Triste", "ansioso" to "Ansioso", "neutral" to "Neutral",
        "bien" to "Bien", "euforico" to "Eufórico"
    )
    val mood = entry.mood ?: "neutral"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = "Ya registraste hoy",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceDim),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(faceIcons[mood] ?: R.drawable.ic_face_neutral),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(
                        text = faceLabels[mood] ?: "Neutral",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (entry.consumed == true)
                        "Consumo: ${entry.substance?.replaceFirstChar { it.uppercase() } ?: "sí"}"
                    else "Sin consumo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                entry.note?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        SolvyxButton(
            text = "Editar registro de hoy",
            onClick = onEditar,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
