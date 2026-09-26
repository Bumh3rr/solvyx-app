package com.solvyx.ui.components.berto

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.solvyx.ui.theme.TealDark
import com.solvyx.ui.theme.TealLight

private val DefaultBertoSize = 92.dp

/** Speech bubble tail corner: the bubble "points" back at Berto on its left. */
private val BubbleShape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp)

/**
 * Berto on the left talking in a bubble on the right: a bold [message] and an optional
 * [supporting] line. [berto] draws him (a Rive pose, a still image…) inside a [bertoSize] box.
 */
@Composable
fun BertoSpeechRow(
    message: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    bertoSize: Dp = DefaultBertoSize,
    berto: @Composable () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(bertoSize), contentAlignment = Alignment.Center) { berto() }
        Spacer(Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceDim, BubbleShape)
                .border(1.dp, TealLight, BubbleShape)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = TealDark
            )
            supporting?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
