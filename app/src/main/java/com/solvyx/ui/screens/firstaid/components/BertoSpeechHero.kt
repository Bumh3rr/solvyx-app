package com.solvyx.ui.screens.firstaid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.ui.screens.firstaid.model.GuideHero
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private val BertoHeroSize = 92.dp

/** Speech bubble tail corner: the bubble "points" back at Berto on its left. */
private val BubbleShape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp)

/** Opening of every guide: Berto talking to the user in first person, before any instruction. */
@Composable
fun BertoSpeechHero(hero: GuideHero, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FirstAidBerto(mood = hero.mood, modifier = Modifier.size(BertoHeroSize))
        Spacer(Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceDim, BubbleShape)
                .border(1.dp, TealLight, BubbleShape)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = hero.message,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = hero.supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
