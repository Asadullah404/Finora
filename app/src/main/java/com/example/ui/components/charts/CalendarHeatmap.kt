package com.example.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculation.FinancialEngine

@Composable
fun CalendarHeatmap(
    yearMonth: String,
    dailyExpenses: Map<Int, Long>, // Day (1..31) -> amountMinorUnits
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    val maxDaily = dailyExpenses.values.maxOrNull()?.coerceAtLeast(1L) ?: 1L

    val daysInMonth = try {
        java.time.YearMonth.parse(yearMonth).lengthOfMonth()
    } catch (_: Exception) {
        31
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Daily Spending Intensity",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            // Intensity Key
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Low", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                listOf(
                    Color(0xFF1E293B),
                    Color(0x55F43F5E),
                    Color(0x99F43F5E),
                    Color(0xFFF43F5E)
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .padding(1.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("High", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tooltip for selected day
        if (selectedDay != null) {
            val amt = dailyExpenses[selectedDay!!] ?: 0L
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
                        "$yearMonth-${String.format(java.util.Locale.US, "%02d", selectedDay)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        FinancialEngine.formatMinorUnits(amt, currencyCode),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (amt > 0) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 7-Column Grid of Day Cells
        val rows = (daysInMonth + 6) / 7
        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (c in 0 until 7) {
                    val day = (r * 7) + c + 1
                    if (day <= daysInMonth) {
                        val amount = dailyExpenses[day] ?: 0L
                        val intensity = if (amount > 0L) (amount.toDouble() / maxDaily.toDouble()).coerceIn(0.1, 1.0) else 0.0

                        val cellColor = when {
                            intensity == 0.0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            intensity < 0.35 -> Color(0x66F43F5E)
                            intensity < 0.75 -> Color(0xAAF43F5E)
                            else -> Color(0xFFF43F5E)
                        }

                        val isSelected = selectedDay == day

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(cellColor)
                                .clickable {
                                    selectedDay = if (selectedDay == day) null else day
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected || amount > 0L) FontWeight.Bold else FontWeight.Normal,
                                color = if (amount > 0L) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
