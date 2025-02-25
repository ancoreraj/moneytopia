package com.dimrnhhh.moneytopia.components.charts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    indicatorWidth: Int = 8,
    spacing: Int = 8,
    indicatorCount: Int = 5,
    isInReverseOrder: Boolean
) {
    val totalDots = pagerState.pageCount
    val currentPage = pagerState.currentPage

    Row(
        modifier = modifier
            .wrapContentHeight()
            .wrapContentWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.dp)
    ) {
        // Calculate the window of visible dots
        val windowSize = indicatorCount
        val windowOffset = when {
            // At the start (right side, since it's reversed)
            currentPage >= totalDots - windowSize -> totalDots - windowSize
            // At the end (left side)
            currentPage <= 0 -> 0
            // In the middle, center the window around the current page
            else -> (currentPage - windowSize / 2).coerceAtLeast(0)
        }

        val range = if (isInReverseOrder) {
            (windowOffset + windowSize - 1) downTo windowOffset
        } else {
            windowOffset until (windowOffset + windowSize)
        }

        for (i in range) {
            if (i in 0 until totalDots) {
                Box(
                    modifier = Modifier
                        .size(indicatorWidth.dp)
                        .clip(CircleShape)
                        .background(
                            if (i == currentPage) activeColor else inactiveColor
                        )
                )
            }
        }
    }
}