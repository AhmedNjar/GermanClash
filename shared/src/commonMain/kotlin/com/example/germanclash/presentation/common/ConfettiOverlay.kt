package com.example.germanclash.presentation.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val angleDegrees: Float,
    val distance: Float,
    val color: Color,
    val radius: Float,
)

private val ConfettiColors = listOf(
    Color(0xFFFFC107), Color(0xFF5B6EF5), Color(0xFF2ECC71), Color(0xFFFF6B6B)
)

/**
 * Fire-and-forget burst from screen center. Increment `triggerKey` (e.g. a
 * counter bumped on every correct answer) to replay it.
 */
@Composable
fun ConfettiOverlay(
    triggerKey: Int,
    modifier: Modifier = Modifier,
    particleCount: Int = 24
) {
    val particles = remember(triggerKey) {
        List(particleCount) {
            ConfettiParticle(
                angleDegrees = Random.nextFloat() * 360f,
                distance = 120f + (Random.nextFloat() * 80f),
                color = ConfettiColors.random(),
                radius = 6f + Random.nextFloat() * 6f
            )
        }
    }
    val progress = remember(triggerKey) { Animatable(0f) }

    LaunchedEffect(triggerKey) {
        if (triggerKey == 0) return@LaunchedEffect
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 700))
    }

    Canvas(modifier = modifier) {
        if (progress.value == 0f) return@Canvas
        val center = Offset(size.width / 2, size.height / 2)
        particles.forEach { particle ->
            val radians = particle.angleDegrees * (PI / 180.0)
            val currentDistance = particle.distance * progress.value
            val offset = Offset(
                x = center.x + (cos(radians) * currentDistance).toFloat(),
                y = center.y + (sin(radians) * currentDistance).toFloat() - (200f * progress.value)
            )
            drawCircle(
                color = particle.color.copy(alpha = 1f - progress.value),
                radius = particle.radius,
                center = offset
            )
        }
    }
}
