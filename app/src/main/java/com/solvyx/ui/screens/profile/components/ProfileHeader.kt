package com.solvyx.ui.screens.profile.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.backend.common.streak.MilestoneProgress
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.diasLabel

private val RingSize = 108.dp
private val RingStroke = 7.dp
private val AvatarSize = 86.dp
private val EditBadgeSize = 30.dp
private const val RingFillMillis = 1100
private const val RingTrackAlpha = 0.22f
private const val OnPrimarySecondaryAlpha = 0.8f
private const val GlassAlpha = 0.15f
private const val FullCircleDegrees = 360f
private const val RingStartAngle = -90f

/**
 * Top of Mi perfil, on the section's teal color: Berto inside a ring that fills toward the next
 * streak milestone, the user's name, and a line that says how close that milestone is. Tapping
 * the avatar or "Editar perfil" opens the edit sheet.
 */
@Composable
fun ProfileHeader(
    nickname: String,
    memberSince: String,
    streak: Int,
    milestone: MilestoneProgress,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
            .padding(horizontal = 20.dp)
            .padding(top = 4.dp, bottom = 22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StreakRingAvatar(progress = milestone.progress, onClick = onEditProfile)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = nickname,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (memberSince.isNotBlank()) {
                    Text(
                        text = "Miembro desde $memberSince",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = OnPrimarySecondaryAlpha)
                    )
                }
                Spacer(Modifier.height(10.dp))
                EditProfilePill(onClick = onEditProfile)
            }
        }
        Spacer(Modifier.height(16.dp))
        MilestoneLine(streak = streak, milestone = milestone)
    }
}

@Composable
private fun StreakRingAvatar(progress: Float, onClick: () -> Unit) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animatedProgress.animateTo(progress, tween(RingFillMillis, easing = FastOutSlowInEasing))
    }
    // The edit badge sits outside the circular clip of the ring, so it isn't cut off.
    Box(modifier = Modifier.size(RingSize)) {
        RingedAvatar(
            progress = animatedProgress.value,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .clickable(role = Role.Button, onClickLabel = "Editar perfil", onClick = onClick)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(EditBadgeSize)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable(role = Role.Button, onClickLabel = "Editar perfil", onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_pencil),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/** Berto on a white disc, circled by a ring filled to [progress] (0..1). */
@Composable
private fun RingedAvatar(progress: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(width = RingStroke.toPx(), cap = StrokeCap.Round)
            val inset = RingStroke.toPx() / 2
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)
            drawArc(Color.White.copy(alpha = RingTrackAlpha), 0f, FullCircleDegrees, false, topLeft, arcSize, style = stroke)
            drawArc(Color.White, RingStartAngle, FullCircleDegrees * progress, false, topLeft, arcSize, style = stroke)
        }
        Box(
            modifier = Modifier
                .size(AvatarSize)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            BertoPoseAnimation(
                pose = BertoPose.CENTER_IDLE_HELLO,
                riveFileRes = R.raw.berto_poses,
                modifier = Modifier
                    .size(AvatarSize - 12.dp)
                    .offset(y = 4.dp),
                fallback = R.drawable.berto_feliz
            )
        }
    }
}

@Composable
private fun EditProfilePill(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = GlassAlpha))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_pencil),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = "Editar perfil",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

@Composable
private fun MilestoneLine(streak: Int, milestone: MilestoneProgress) {
    val text = when {
        streak == 0 -> "Tu racha empieza con tu próximo registro en Mi camino."
        milestone.progress >= 1f && streak >= milestone.next ->
            "Superaste todos los hitos de racha. Sigue así."
        else -> {
            val remaining = milestone.next - streak
            "Te faltan $remaining ${diasLabel(remaining)} para tu hito de ${milestone.next} días."
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = GlassAlpha), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_flame),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
    }
}
