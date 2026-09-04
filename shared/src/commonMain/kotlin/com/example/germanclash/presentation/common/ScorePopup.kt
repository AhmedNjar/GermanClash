package com.example.germanclash.presentation.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.example.germanclash.presentation.theme.GameColors
import kotlin.math.roundToInt

/**
 * A floating points indicator (+100) that rises from a button when
 * an answer is correct.
 */
@Composable
fun ScorePopup(
    points: Int,
    anchor: Offset,
    onFinished: () -> Unit
) {
    val alpha = remember { Animatable(1f) }
    val offsetY = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animate rising and fading out simultaneously
        val animation = tween<Float>(durationMillis = 800)
        
        // Rise by 120 pixels
        offsetY.animateTo(-120f, animationSpec = animation)
        // Fade out
        alpha.animateTo(0f, animationSpec = animation)
        
        onFinished()
    }

    if (alpha.value > 0f) {
        Text(
            text = "+$points",
            color = GameColors.CorrectGreen.copy(alpha = alpha.value),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = anchor.x.roundToInt(),
                        y = (anchor.y + offsetY.value).roundToInt()
                    )
                }
        )
    }
}
