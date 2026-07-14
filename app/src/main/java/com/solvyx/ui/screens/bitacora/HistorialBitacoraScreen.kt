package com.solvyx.ui.screens.bitacora

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.data.local.entity.JournalEntity
import com.solvyx.ui.components.common.SolvyxBackButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistorialBitacoraScreen(
    viewModel: RegistroViewModel,
    onBack: () -> Unit
) {
    val registros by viewModel.historial.collectAsState()
    val totalRegistros = registros.size
    val sinConsumo = registros.count { !it.consumed }
    val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "MX"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SolvyxBackButton(onClick = onBack)
            Text(
                "Historial de Registros",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.size(48.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ResumenStatItem(totalRegistros.toString(), "Registros")
            ResumenStatItem(sinConsumo.toString(), "Sin consumo")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(registros) { registro ->
                HistorialRegistroCard(
                    registro = registro,
                    fechaLabel = dateFormat.format(Date(registro.date))
                        .replaceFirstChar { it.uppercase() }
                )
            }
        }
    }
}

@Composable
private fun ResumenStatItem(valor: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            valor,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = Color.White
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HistorialRegistroCard(registro: JournalEntity, fechaLabel: String) {
    val faceIcons = mapOf(
        "triste"   to R.drawable.ic_face_sad,
        "ansioso"  to R.drawable.ic_face_anxious,
        "neutral"  to R.drawable.ic_face_neutral,
        "bien"     to R.drawable.ic_face_happy,
        "euforico" to R.drawable.ic_face_euphoric
    )
    val faceLabels = mapOf(
        "triste" to "Triste", "ansioso" to "Ansioso",
        "neutral" to "Neutral", "bien" to "Bien", "euforico" to "Eufórico"
    )
    val sosRed = Color(0xFFE24B4A)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceDim),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    fechaLabel,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (registro.consumed) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(sosRed.copy(alpha = 0.10f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "Consumo: ${registro.substance?.replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = sosRed
                        )
                    }
                } else {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "Sin consumo",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                thickness = 0.5.dp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            faceIcons[registro.mood] ?: R.drawable.ic_face_neutral
                        ),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        faceLabels[registro.mood] ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (registro.note != null) {
                        Text(
                            registro.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
