package com.example.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class TrendPoint(
    val label: String,
    val valueMinorUnits: Long
)

@Composable
fun TrendLineChart(
    points: List<TrendPoint>,
    lineColor: Color = Color(0xFF10B981),
    gradientStartColor: Color = Color(0x3310B981),
    modifier: Modifier = Modifier
) {
    if (points.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Add more transactions to view trend line",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val maxVal = points.maxOfOrNull { it.valueMinorUnits }?.coerceAtLeast(1L)?.toFloat() ?: 1f
        val minVal = points.minOfOrNull { it.valueMinorUnits }?.coerceAtLeast(0L)?.toFloat() ?: 0f
        val range = (maxVal - minVal).coerceAtLeast(1f)

        val chartHeight = size.height - 16.dp.toPx()
        val stepX = size.width / (points.size - 1)

        val strokePath = Path()
        val fillPath = Path()

        val coordinates = points.mapIndexed { index, point ->
            val norm = (point.valueMinorUnits.toFloat() - minVal) / range
            val y = chartHeight - (norm * chartHeight * animationProgress.value) + 8.dp.toPx()
            val x = index * stepX
            Offset(x, y)
        }

        strokePath.moveTo(coordinates[0].x, coordinates[0].y)
        fillPath.moveTo(coordinates[0].x, chartHeight + 8.dp.toPx())
        fillPath.lineTo(coordinates[0].x, coordinates[0].y)

        for (i in 1 until coordinates.size) {
            val p0 = coordinates[i - 1]
            val p1 = coordinates[i]
            val midX = (p0.x + p1.x) / 2f
            strokePath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
            fillPath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
        }

        fillPath.lineTo(coordinates.last().x, chartHeight + 8.dp.toPx())
        fillPath.close()

        // Draw gradient area below line
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(gradientStartColor, Color.Transparent),
                startY = 0f,
                endY = chartHeight + 8.dp.toPx()
            )
        )

        // Draw line
        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw endpoint dots
        coordinates.forEach { pt ->
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = lineColor,
                radius = 2.dp.toPx(),
                center = pt
            )
        }
    }
}
