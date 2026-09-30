package com.solvyx.ui.screens.home.stage

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.common.SolvyxButton
import com.solvyx.ui.components.common.SolvyxCard
import com.solvyx.ui.components.common.SolvyxTextButton
import com.solvyx.ui.theme.TealDark

/**
 * The idea Berto is suggesting. The thin bar fills during the ≈5 s Berto spends explaining it on
 * the stage; the card stays afterwards so the user can still act on it or ask for another.
 */
@Composable
fun BertoSuggestionCard(
    suggestion: BertoSuggestion,
    suggestionKey: Long,
    onAction: () -> Unit,
    onAnotherIdea: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    SolvyxCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Berto sugiere",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        painterResource(R.drawable.ic_circle_x),
                        contentDescription = "Cerrar sugerencia",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            AnimatedContent(
                targetState = suggestion,
                transitionSpec = {
                    (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut())
                },
                label = "SuggestionSwap"
            ) { shown -> SuggestionBody(shown) }
            ExplainingBar(suggestionKey, Modifier.padding(end = 8.dp, top = 12.dp))
            Row(
                modifier = Modifier.padding(top = 12.dp, end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SolvyxButton(text = suggestion.actionLabel, onClick = onAction, modifier = Modifier.weight(1f))
                SolvyxTextButton(text = "Otra idea", onClick = onAnotherIdea)
            }
        }
    }
}

@Composable
private fun SuggestionBody(suggestion: BertoSuggestion) {
    Row(Modifier.padding(end = 8.dp), verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(suggestion.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                suggestion.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark
            )
            Text(
                suggestion.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** Fills while Berto explains, then fades away. */
@Composable
private fun ExplainingBar(suggestionKey: Long, modifier: Modifier = Modifier) {
    val progress = remember(suggestionKey) { Animatable(0f) }
    LaunchedEffect(suggestionKey) {
        progress.animateTo(1f, tween(SUGGESTING_MS.toInt(), easing = LinearEasing))
    }
    AnimatedVisibility(visible = progress.value < 1f, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress.value)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
