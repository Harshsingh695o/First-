package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.TouchEffectType
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

data class ActiveTouch(
    val id: Long,
    val x: Float,
    val y: Float,
    val effectType: TouchEffectType,
    val color: Color
)

@Composable
fun TouchFxRenderer(
    activeTouches: List<ActiveTouch>,
    onComplete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    for (touch in activeTouches) {
        SingleTouchEffect(touch = touch, onComplete = { onComplete(touch.id) })
    }
}

@Composable
private fun SingleTouchEffect(
    touch: ActiveTouch,
    onComplete: () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(touch.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = LinearEasing)
        )
        onComplete()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val p = progress.value
        val center = Offset(touch.x, touch.y)

        when (touch.effectType) {
            TouchEffectType.RIPPLE -> drawRipples(center, p, touch.color)
            TouchEffectType.PARTICLES -> drawParticles(center, p, touch.color)
            TouchEffectType.SPARKLE -> drawSparkles(center, p, touch.color)
            TouchEffectType.PULSE -> drawPulse(center, p, touch.color)
            TouchEffectType.NONE -> {}
        }
    }
}

private fun DrawScope.drawRipples(center: Offset, progress: Float, baseColor: Color) {
    val maxRadius = size.minDimension * 0.28f
    val currentRadius = progress * maxRadius
    val alpha = (1f - progress).coerceIn(0f, 1f)

    // Outer primary wave
    drawCircle(
        color = baseColor.copy(alpha = alpha * 0.7f),
        radius = currentRadius,
        center = center,
        style = Stroke(width = (4f * (1f - progress)).coerceAtLeast(1f))
    )

    // Secondary delayed wave
    if (progress > 0.2f) {
        val p2 = (progress - 0.2f) / 0.8f
        val r2 = p2 * maxRadius * 0.8f
        val a2 = (1f - p2).coerceIn(0f, 1f)
        drawCircle(
            color = baseColor.copy(alpha = a2 * 0.4f),
            radius = r2,
            center = center,
            style = Stroke(width = 2.5f)
        )
    }

    // Core glow dot
    drawCircle(
        color = baseColor.copy(alpha = alpha * 0.9f),
        radius = 8f * (1f - progress),
        center = center
    )
}

private fun DrawScope.drawParticles(center: Offset, progress: Float, baseColor: Color) {
    val numParticles = 14
    val alpha = (1f - progress).coerceIn(0f, 1f)
    val distance = progress * (size.minDimension * 0.22f)

    for (i in 0 until numParticles) {
        val angle = (i * (360f / numParticles)) * (Math.PI / 180f).toFloat()
        val px = center.x + distance * cos(angle)
        val py = center.y + distance * sin(angle)
        val particleRadius = (6f * (1f - progress * 0.6f)).coerceAtLeast(1.5f)

        drawCircle(
            color = if (i % 2 == 0) baseColor.copy(alpha = alpha) else Color.White.copy(alpha = alpha * 0.85f),
            radius = particleRadius,
            center = Offset(px, py)
        )
    }
}

private fun DrawScope.drawSparkles(center: Offset, progress: Float, baseColor: Color) {
    val numRays = 8
    val alpha = (1f - progress).coerceIn(0f, 1f)
    val rayLength = progress * (size.minDimension * 0.25f)

    for (i in 0 until numRays) {
        val angle = (i * (360f / numRays) + (progress * 30f)) * (Math.PI / 180f).toFloat()
        val startDist = progress * 15f
        val startX = center.x + startDist * cos(angle)
        val startY = center.y + startDist * sin(angle)
        val endX = center.x + rayLength * cos(angle)
        val endY = center.y + rayLength * sin(angle)

        drawLine(
            color = baseColor.copy(alpha = alpha * 0.9f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = (3.5f * (1f - progress)).coerceAtLeast(1f)
        )
    }
}

private fun DrawScope.drawPulse(center: Offset, progress: Float, baseColor: Color) {
    val radius = progress * (size.minDimension * 0.35f)
    val alpha = (1f - progress).coerceIn(0f, 1f) * 0.5f

    drawCircle(
        color = baseColor.copy(alpha = alpha),
        radius = radius,
        center = center
    )
}
