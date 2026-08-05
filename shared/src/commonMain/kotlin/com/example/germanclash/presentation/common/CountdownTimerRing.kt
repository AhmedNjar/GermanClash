package com.example.germanclash.presentation.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.germanclash.presentation.theme.GameColors

/**
 * `progress` is 1f (full time left) to 0f (expired). Color shifts
 * green -> yellow -> red as it drains.
 */
@Composable
fun CountdownTimerRing(
    progress: Float,
    secondsRemaining: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "timerProgress"
    )

    val ringColor by animateColorAsState(
        targetValue = when {
            progress > 0.5f -> GameColors.TimerGreen
            progress > 0.2f -> GameColors.TimerYellow
            else -> GameColors.TimerRed
        },
        animationSpec = tween(durationMillis = 300),
        label = "timerColor"
    )

    Box(modifier = modifier.size(64.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(64.dp)) {
            val strokeWidth = 6.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            drawArc(
                color = ringColor.copy(alpha = 0.25f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                size = arcSize,
                topLeft = topLeft
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = arcSize,
                topLeft = topLeft
            )
        }
        Text(text = "$secondsRemaining", color = ringColor)
    }
}
