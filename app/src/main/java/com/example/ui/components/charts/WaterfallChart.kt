package com.example.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculation.FinancialEngine

@Composable
fun WaterfallChart(
    openingBalance: Long,
    totalIncome: Long,
    totalExpenses: Long,
    closingBalance: Long,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val maxVal = maxOf(
        openingBalance,
        openingBalance + totalIncome,
        closingBalance,
        totalExpenses,
        1L
    ).toFloat()

    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryText = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Cash Flow Waterfall",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Net: ${FinancialEngine.formatMinorUnits(closingBalance - openingBalance, currencyCode, includeDecimals = false)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (closingBalance >= openingBalance) Color(0xFF10B981) else Color(0xFFF43F5E)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val chartHeight = size.height - 28.dp.toPx()
            val totalSteps = 4 // Opening, Income, Expenses, Closing
            val colWidth = size.width / totalSteps
            val barWidth = colWidth * 0.55f

            val labels = listOf("Opening", "+ Income", "- Expense", "Closing")
            val colors = listOf(
                Color(0xFF3B82F6), // Opening (Blue)
                Color(0xFF10B981), // Income (Emerald)
                Color(0xFFF43F5E), // Expense (Rose)
                Color(0xFF8B5CF6)  // Closing (Purple)
            )

            // Step 1: Opening Bar [0 -> openingBalance]
            val x0 = (0 * colWidth) + (colWidth - barWidth) / 2f
            val h0 = (openingBalance.toFloat() / maxVal) * chartHeight
            val y0 = chartHeight - h0
            drawRoundRect(
                color = colors[0],
                topLeft = Offset(x0, y0),
                size = Size(barWidth, h0.coerceAtLeast(4.dp.toPx())),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Step 2: Income Bar [opening -> opening + income]
            val x1 = (1 * colWidth) + (colWidth - barWidth) / 2f
            val h1 = (totalIncome.toFloat() / maxVal) * chartHeight
            val y1 = y0 - h1
            drawRoundRect(
                color = colors[1],
                topLeft = Offset(x1, y1),
                size = Size(barWidth, h1.coerceAtLeast(4.dp.toPx())),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Step 3: Expense Bar [opening + income -> opening + income - expenses]
            val x2 = (2 * colWidth) + (colWidth - barWidth) / 2f
            val h2 = (totalExpenses.toFloat() / maxVal) * chartHeight
            val y2 = y1 // Start from top of income and go downwards
            drawRoundRect(
                color = colors[2],
                topLeft = Offset(x2, y2),
                size = Size(barWidth, h2.coerceAtLeast(4.dp.toPx())),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Step 4: Closing Balance Bar [0 -> closingBalance]
            val x3 = (3 * colWidth) + (colWidth - barWidth) / 2f
            val h3 = (closingBalance.toFloat().coerceAtLeast(0f) / maxVal) * chartHeight
            val y3 = chartHeight - h3
            drawRoundRect(
                color = colors[3],
                topLeft = Offset(x3, y3),
                size = Size(barWidth, h3.coerceAtLeast(4.dp.toPx())),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Connective dashed guide lines
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            // Opening top to income bottom
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(x0 + barWidth, y0),
                end = Offset(x1, y0),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect
            )
            // Income top to expense top
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(x1 + barWidth, y1),
                end = Offset(x2, y1),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect
            )

            // Labels under bars
            for (i in 0 until totalSteps) {
                val cx = (i * colWidth) + (colWidth / 2f)
                val measured = textMeasurer.measure(
                    text = labels[i],
                    style = TextStyle(fontSize = 10.sp, color = labelColor)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = labels[i],
                    topLeft = Offset(cx - (measured.size.width / 2f), chartHeight + 6.dp.toPx()),
                    style = TextStyle(fontSize = 10.sp, color = labelColor)
                )
            }
        }
    }
}
