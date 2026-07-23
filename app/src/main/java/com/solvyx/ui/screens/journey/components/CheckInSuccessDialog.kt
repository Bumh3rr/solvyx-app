package com.solvyx.ui.screens.journey.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton

@Composable
fun CheckInSuccessDialog(
    mood: String?,
    used: Boolean,
    substance: String?,
    onDismiss: () -> Unit
) {
    val faceIcons = mapOf(
        "triste"   to R.drawable.ic_face_sad,
        "ansioso"  to R.drawable.ic_face_anxious,
        "neutral"  to R.drawable.ic_face_neutral,
        "bien"     to R.drawable.ic_face_happy,
        "euforico" to R.drawable.ic_face_euphoric
    )
    val emoLabels = mapOf(
        "triste" to "Triste", "ansioso" to "Ansioso",
        "neutral" to "Neutral", "bien" to "Bien", "euforico" to "Eufórico"
    )
    val message = when (mood) {
        "bien"     -> "¡Qué bueno escuchar eso!\nSigue cuidándote así."
        "euforico" -> "¡Qué energía! Aprovéchala\ncon sabiduría."
        "neutral"  -> "Un día tranquilo también\ncuenta. Gracias por registrar."
        "triste"   -> "Registrar cómo te sientes\ntoma valentía. Bien hecho."
        "ansioso"  -> "Reconocer la ansiedad\nes el primer paso. ¡Lo lograste!"
        else       -> "Gracias por ser honesto\ncontigo mismo hoy."
    }
    val sosRed = Color(0xFFE24B4A)

    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.75f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "DialogScale"
    )
    LaunchedEffect(Unit) { visible = true }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .scale(scale),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box {
                Column {
                    // ── Header teal ───────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(164.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.berto_feliz),
                            contentDescription = null,
                            modifier = Modifier
                                .size(130.dp)
                                .offset(y = (-8).dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // ── Body ──────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(30.dp))

                        Text(
                            "¡Registro guardado!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(20.dp))

                        // ── Summary ───────────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                .padding(vertical = 14.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mood
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(
                                        faceIcons[mood] ?: R.drawable.ic_face_neutral
                                    ),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    emoLabels[mood] ?: "—",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                Modifier
                                    .width(0.5.dp)
                                    .height(36.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            )

                            // Use
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(
                                        if (used) R.drawable.ic_alert_circle
                                        else R.drawable.ic_check_circle
                                    ),
                                    contentDescription = null,
                                    tint = if (used) sosRed
                                           else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    if (used)
                                        substance?.replaceFirstChar { it.uppercase() } ?: "Sí"
                                    else "Sin consumo",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        SolvyxButton(
                            text = "¡Todo listo!",
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(24.dp))
                    }
                }

                // ── Badge checkmark overlapping the header ───
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = 140.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
