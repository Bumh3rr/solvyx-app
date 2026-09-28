package com.solvyx.ui.screens.journey.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.StaggeredAppear
import com.solvyx.ui.components.common.moodOption
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private const val PREVIEW_DAYS = 7

/**
 * Door to "Mi diario" from the Progress tab. The dots are the moods of the last logged days
 * (newest on the right), so the card already hints at what is inside.
 */
@Composable
fun DiaryEntryCard(recentMoods: List<String?>, registeredDays: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SolvyxCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BertoPoseAnimation(
                pose = BertoPose.CENTER_IDLE_TO_RIGHT,
                riveFileRes = R.raw.berto_poses,
                modifier = Modifier.size(52.dp),
                fallback = R.drawable.berto_dedo_der
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Mi diario",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = TealDark
                )
                Text(
                    if (registeredDays == 1) "1 día para recordar" else "$registeredDays días para recordar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.size(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    recentMoods.takeLast(PREVIEW_DAYS).forEachIndexed { index, mood ->
                        StaggeredAppear(index = index) {
                            Box(
                                Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(moodOption(mood).color)
                            )
                        }
                    }
                }
            }
            Icon(painterResource(R.drawable.ic_chevron_right), null, tint = TealLight, modifier = Modifier.size(18.dp))
        }
    }
}
