package com.solvyx.ui.components.common

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            IndicatorDot(
                isSelected = index == currentPage,
                activeColor = MaterialTheme.colorScheme.onBackground
            )
            if (index < pageCount - 1) {
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun IndicatorDot(isSelected: Boolean, activeColor: Color) {
    val width by animateDpAsState(
        targetValue = if (isSelected) 40.dp else 12.dp,
        label = "dot_width"
    )
    Box(
        modifier = Modifier
            .width(width)
            .height(12.dp)
            .shadow(2.dp, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) activeColor else Color.White)
    )
}
