package com.example.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculation.FinancialEngine

data class MonthlyBarData(
    val monthLabel: String, // e.g. "May", "Jun", "Oct"
    val incomeMinorUnits: Long,
    val expenseMinorUnits: Long
)

@Composable
fun IncomeExpenseBarChart(
    data: List<MonthlyBarData>,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No historical comparison data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val animationProgress = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
    }

    val incomeColor = Color(0xFF10B981)
    val expenseColor = Color(0xFFF43F5E)
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(modifier = Modifier.size(10.dp), shape = CircleShape, color = incomeColor) {}
            Spacer(modifier = Modifier.width(6.dp))
            Text("Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.width(20.dp))

            Surface(modifier = Modifier.size(10.dp), shape = CircleShape, color = expenseColor) {}
            Spacer(modifier = Modifier.width(6.dp))
            Text("Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
        }

        // Active Tooltip Banner if tapped
        if (selectedIndex != null && selectedIndex!! in data.indices) {
            val item = data[selectedIndex!!]
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.monthLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row {
                        Text(
                            "+${FinancialEngine.formatMinorUnits(item.incomeMinorUnits, currencyCode, includeDecimals = false)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = incomeColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "-${FinancialEngine.formatMinorUnits(item.expenseMinorUnits, currencyCode, includeDecimals = false)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = expenseColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val groupWidth = size.width / data.size
                        val idx = (offset.x / groupWidth).toInt()
                        selectedIndex = if (idx in data.indices) idx else null
                    }
                }
        ) {
            val maxVal = data.maxOfOrNull { maxOf(it.incomeMinorUnits, it.expenseMinorUnits) }?.coerceAtLeast(100L) ?: 100L
            val chartHeight = size.height - 30.dp.toPx()
            val groupWidth = size.width / data.size
            val barWidth = (groupWidth * 0.32f).coerceAtMost(22.dp.toPx())
            val barSpacing = 4.dp.toPx()

            // Horizontal Grid Lines
            val gridSteps = 3
            for (i in 0..gridSteps) {
                val y = chartHeight * (i.toFloat() / gridSteps.toFloat())
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            for (i in data.indices) {
                val item = data[i]
                val groupCenterX = (i * groupWidth) + (groupWidth / 2f)

                val incomeHeight = (item.incomeMinorUnits.toFloat() / maxVal.toFloat()) * chartHeight * animationProgress.value
                val expenseHeight = (item.expenseMinorUnits.toFloat() / maxVal.toFloat()) * chartHeight * animationProgress.value

                val incomeLeft = groupCenterX - barWidth - (barSpacing / 2f)
                val expenseLeft = groupCenterX + (barSpacing / 2f)

                // Draw Income Bar
                drawRoundRect(
                    color = incomeColor,
                    topLeft = Offset(incomeLeft, chartHeight - incomeHeight),
                    size = Size(barWidth, incomeHeight.coerceAtLeast(2.dp.toPx())),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Draw Expense Bar
                drawRoundRect(
                    color = expenseColor,
                    topLeft = Offset(expenseLeft, chartHeight - expenseHeight),
                    size = Size(barWidth, expenseHeight.coerceAtLeast(2.dp.toPx())),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Month Label
                val measuredText = textMeasurer.measure(
                    text = item.monthLabel,
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = if (selectedIndex == i) Color.White else labelColor,
                        fontWeight = if (selectedIndex == i) FontWeight.Bold else FontWeight.Normal
                    )
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = item.monthLabel,
                    topLeft = Offset(
                        groupCenterX - (measuredText.size.width / 2f),
                        chartHeight + 8.dp.toPx()
                    ),
                    style = TextStyle(fontSize = 11.sp, color = labelColor)
                )
            }
        }
    }
}
