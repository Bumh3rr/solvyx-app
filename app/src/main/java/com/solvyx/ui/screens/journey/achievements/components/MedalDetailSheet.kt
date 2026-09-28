package com.solvyx.ui.screens.journey.achievements.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.theme.TealDark
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DateFormat = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("es", "MX"))
private const val FLIP_START_DEGREES = 180f
private const val FLIP_MS = 700
private const val CAMERA_DISTANCE = 12f

/** Everything the detail sheet shows, for a streak achievement or a diary badge alike. */
data class MedalInfo(
    val title: String,
    val description: String,
    @DrawableRes val icon: Int,
    val color: Color,
    val unlocked: Boolean,
    val current: Int,
    val target: Int,
    val unit: String,
    val unlockedOn: LocalDate? = null
) {
    val progress: Float get() = if (target == 0) 1f else (current.toFloat() / target).coerceIn(0f, 1f)
}

private fun bertoLine(info: MedalInfo): String = when {
    info.unlocked -> "¡Estoy muy orgulloso de ti! Esto lo ganaste tú."
    info.target - info.current == 1 -> "¡Ya casi! Solo te falta uno."
    info.current == 0 -> "Este lo vamos a conseguir juntos, paso a paso."
    else -> "Vas muy bien. Cada día cuenta."
}

/** A medal's detail: it flips in, then says when it was earned or how much is left. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedalDetailSheet(info: MedalInfo, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FlippingMedal(info)
            Spacer(Modifier.height(16.dp))
            Text(
                info.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark,
                textAlign = TextAlign.Center
            )
            Text(
                info.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            if (info.unlocked) UnlockedStatus(info) else ProgressStatus(info)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BertoPoseAnimation(
                    pose = if (info.unlocked) BertoPose.CENTER_IDLE_HELLO else BertoPose.CENTER_IDLE,
                    riveFileRes = R.raw.berto_poses,
                    modifier = Modifier.size(48.dp),
                    fallback = if (info.unlocked) R.drawable.berto_feliz else R.drawable.berto_tranquilo
                )
                Spacer(Modifier.width(10.dp))
                Text(bertoLine(info), style = MaterialTheme.typography.bodyMedium, color = TealDark)
            }
        }
    }
}

@Composable
private fun FlippingMedal(info: MedalInfo) {
    val flip = remember { Animatable(FLIP_START_DEGREES) }
    val scale = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        launch { flip.animateTo(0f, tween(FLIP_MS, easing = FastOutSlowInEasing)) }
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    Medal(
        icon = info.icon,
        color = info.color,
        unlocked = info.unlocked,
        progress = info.progress,
        size = 132.dp,
        modifier = Modifier.graphicsLayer {
            rotationY = flip.value
            scaleX = scale.value
            scaleY = scale.value
            cameraDistance = CAMERA_DISTANCE * density
        }
    )
}

@Composable
private fun UnlockedStatus(info: MedalInfo) {
    Text(
        info.unlockedOn?.let { "Lo lograste el ${it.format(DateFormat)}" } ?: "Desbloqueado",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = info.color,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ProgressStatus(info: MedalInfo) {
    val fill = remember { Animatable(0f) }
    LaunchedEffect(info.progress) { fill.animateTo(info.progress, tween(900, easing = FastOutSlowInEasing)) }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Text(
                "Llevas ${info.current} de ${info.target} ${info.unit}".trim(),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = TealDark,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Te faltan ${info.target - info.current}",
                style = MaterialTheme.typography.labelLarge,
                color = info.color
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(info.color.copy(alpha = 0.15f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fill.value)
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(info.color)
            )
        }
    }
}
