package com.example.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculation.FinancialEngine
import com.example.model.CategorySpending
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DonutChart(
    items: List<CategorySpending>,
    totalAmountMinorUnits: Long,
    currencyCode: String,
    modifier: Modifier = Modifier,
    onCategoryClick: ((CategorySpending) -> Unit)? = null
) {
    if (items.isEmpty() || totalAmountMinorUnits <= 0L) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No category expense data for this period",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(items) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .pointerInput(items) {
                        detectTapGestures { tapOffset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = tapOffset.x - center.x
                            val dy = tapOffset.y - center.y
                            val dist = sqrt(dx * dx + dy * dy)
                            val outerRadius = size.width / 2f
                            val innerRadius = outerRadius - 40.dp.toPx()

                            if (dist in innerRadius..outerRadius) {
                                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (angle < 0) angle += 360f
                                // Shift by start angle -90
                                angle = (angle + 90f) % 360f

                                var currentAngle = 0f
                                for (i in items.indices) {
                                    val sweep = (items[i].percentageOfTotal.toFloat() / 100f) * 360f
                                    if (angle in currentAngle..(currentAngle + sweep)) {
                                        selectedIndex = if (selectedIndex == i) null else i
                                        onCategoryClick?.invoke(items[i])
                                        break
                                    }
                                    currentAngle += sweep
                                }
                            } else {
                                selectedIndex = null
                            }
                        }
                    }
            ) {
                val strokeWidth = 32.dp.toPx()
                val selectedStrokeWidth = 38.dp.toPx()
                val diameter = size.minDimension - selectedStrokeWidth
                val topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                )

                var currentStartAngle = -90f

                for (i in items.indices) {
                    val item = items[i]
                    val sweepAngle = (item.percentageOfTotal.toFloat() / 100f) * 360f * animationProgress.value
                    val isSelected = selectedIndex == i

                    val color = try {
                        Color(android.graphics.Color.parseColor(item.colorHex))
                    } catch (_: Exception) {
                        Color(0xFF10B981)
                    }

                    drawArc(
                        color = color,
                        startAngle = currentStartAngle,
                        sweepAngle = sweepAngle - if (items.size > 1) 1.5f else 0f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(
                            width = if (isSelected) selectedStrokeWidth else strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )

                    currentStartAngle += sweepAngle
                }
            }

            // Center Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                if (selectedIndex != null && selectedIndex!! in items.indices) {
                    val selected = items[selectedIndex!!]
                    Text(
                        text = selected.categoryName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = FinancialEngine.formatMinorUnits(selected.totalMinorUnits, currencyCode, includeDecimals = false),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = String.format(java.util.Locale.US, "%.1f%%", selected.percentageOfTotal),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "TOTAL SPENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = FinancialEngine.formatMinorUnits(totalAmountMinorUnits, currencyCode, includeDecimals = false),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${items.size} Categories",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend Chips
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.take(8).forEachIndexed { index, cat ->
                val isSelected = selectedIndex == index
                val color = try {
                    Color(android.graphics.Color.parseColor(cat.colorHex))
                } catch (_: Exception) {
                    Color(0xFF10B981)
                }

                Surface(
                    shape = CircleShape,
                    color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) color else Color.Transparent
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = {
                        selectedIndex = if (selectedIndex == index) null else index
                        onCategoryClick?.invoke(cat)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = CircleShape,
                            color = color
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${cat.categoryName} (${cat.percentageOfTotal.toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
