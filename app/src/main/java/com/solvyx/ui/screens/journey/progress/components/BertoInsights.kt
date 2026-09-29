package com.solvyx.ui.screens.journey.progress.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solvyx.R
import com.solvyx.ui.components.berto.BertoPose
import com.solvyx.ui.components.berto.BertoPoseAnimation
import com.solvyx.ui.components.common.PageIndicator
import com.solvyx.ui.theme.TealDark
import kotlin.math.absoluteValue

private const val MIN_PAGE_ALPHA = 0.5f
private const val MIN_PAGE_SCALE = 0.94f

/** A different Berto pose per card, so swiping feels like he keeps talking. */
private val InsightPoses = listOf(BertoPose.CENTER_IDLE_TO_RIGHT, BertoPose.CENTER_IDLE_HELLO, BertoPose.CENTER_IDLE)

/** "Lo que Berto nota": swipeable cards with what the diary shows, one idea per card. */
@Composable
fun BertoInsights(insights: List<String>, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState { insights.size }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Lo que Berto nota",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = TealDark
        )
        Spacer(Modifier.height(8.dp))
        HorizontalPager(state = pagerState, pageSpacing = 12.dp) { page ->
            val offset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = 1f - (1f - MIN_PAGE_ALPHA) * offset
                        val scale = 1f - (1f - MIN_PAGE_SCALE) * offset
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BertoPoseAnimation(
                    pose = InsightPoses[page % InsightPoses.size],
                    riveFileRes = R.raw.berto_poses,
                    modifier = Modifier.size(56.dp),
                    fallback = R.drawable.berto_dedo_der
                )
                Spacer(Modifier.width(12.dp))
                Text(insights[page], style = MaterialTheme.typography.bodyMedium, color = TealDark)
            }
        }
        if (insights.size > 1) {
            Spacer(Modifier.height(8.dp))
            PageIndicator(
                pageCount = insights.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
