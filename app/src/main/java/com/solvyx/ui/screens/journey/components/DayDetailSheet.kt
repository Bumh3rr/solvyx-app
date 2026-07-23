package com.solvyx.ui.screens.journey.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.data.model.JournalEntry
import com.solvyx.ui.theme.TealDark
import java.time.format.DateTimeFormatter
import java.util.Locale

private val faceIcons = mapOf(
    "triste" to R.drawable.ic_face_sad,
    "ansioso" to R.drawable.ic_face_anxious,
    "neutral" to R.drawable.ic_face_neutral,
    "bien" to R.drawable.ic_face_happy,
    "euforico" to R.drawable.ic_face_euphoric
)
private val faceLabels = mapOf(
    "triste" to "Triste", "ansioso" to "Ansioso",
    "neutral" to "Neutral", "bien" to "Bien", "euforico" to "Eufórico"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailSheet(entry: JournalEntry, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val formattedDate = entry.date
        .format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "MX")))
        .replaceFirstChar { it.uppercase() }
    val mood = entry.mood ?: "neutral"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TealDark
            )
            Spacer(Modifier.height(16.dp))

            // Mood
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(faceIcons[mood] ?: R.drawable.ic_face_neutral),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.size(10.dp))
                Text(
                    text = faceLabels[mood] ?: "Neutral",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(12.dp))

            // Use
            DetailRow(
                label = "Consumo",
                value = if (entry.consumed == true)
                    "Sí — ${entry.substance?.replaceFirstChar { it.uppercase() } ?: "sustancia"}"
                else "Sin consumo"
            )
            entry.cantidadAprox?.takeIf { it.isNotBlank() }?.let {
                DetailRow(label = "Cantidad", value = it)
            }
            entry.notaContexto?.takeIf { it.isNotBlank() }?.let {
                DetailRow(label = "Contexto", value = it)
            }
            entry.note?.takeIf { it.isNotBlank() }?.let {
                DetailRow(label = "Nota", value = it)
            }
            if (entry.metaLograda) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_circle),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "Meta del día cumplida",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(top = 10.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
