package com.solvyx.ui.screens.journey.diary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.AnimatedCountText
import com.solvyx.ui.screens.journey.diary.DiarySummary
import com.solvyx.ui.screens.journey.diary.bertoRecap
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealPrimary

private val HeroShape = RoundedCornerShape(28.dp)
private val BubbleShape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp)

/** Top of "Mi diario": Berto waving, three counters that count up, and his read of the diary. */
@Composable
fun DiaryHero(summary: DiarySummary, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeroShape)
            .background(Brush.verticalGradient(listOf(TealPrimary, TealDark)))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BertoPoseAnimation(
                pose = BertoPose.CENTER_IDLE_HELLO,
                riveFileRes = R.raw.berto_poses,
                modifier = Modifier.size(84.dp),
                fallback = R.drawable.berto_saludando
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = bertoRecap(summary),
                style = MaterialTheme.typography.bodyMedium,
                color = TealDark,
                modifier = Modifier
                    .weight(1f)
                    .clip(BubbleShape)
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HeroStat(value = summary.registeredDays, label = "días escritos", modifier = Modifier.weight(1f))
            HeroStat(value = summary.cleanDays, label = "días limpios", modifier = Modifier.weight(1f))
            HeroStat(value = summary.bestStreak, label = "mejor racha", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun HeroStat(value: Int, label: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedCountText(
            value = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = Color.White
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
    }
}
