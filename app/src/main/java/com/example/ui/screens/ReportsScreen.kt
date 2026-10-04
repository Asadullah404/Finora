package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculation.FinancialEngine
import com.example.ui.FinoraViewModel
import com.example.ui.components.CategoryIconHelper
import com.example.ui.components.MonthYearSelector
import com.example.ui.components.charts.CalendarHeatmap
import com.example.ui.components.charts.DonutChart
import com.example.ui.components.charts.IncomeExpenseBarChart
import com.example.ui.components.charts.MonthlyBarData
import com.example.ui.components.charts.TrendLineChart
import com.example.ui.components.charts.TrendPoint
import com.example.ui.components.charts.WaterfallChart
import java.time.YearMonth

@Composable
fun ReportsScreen(
    viewModel: FinoraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedYm by viewModel.selectedYearMonth.collectAsState()
    val summary by viewModel.financialSummary.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()

    // Build historical comparison bar data (past 4-6 months)
    val barChartData = remember(allTransactions, selectedYm) {
        val current = try { YearMonth.parse(selectedYm) } catch (_: Exception) { YearMonth.now() }
        val months = (3 downTo 0).map { current.minusMonths(it.toLong()) }
        months.map { ym ->
            val ymStr = ym.toString()
            val mIncome = allTransactions.filter { it.transactionYearMonth == ymStr && it.isIncome }.sumOf { it.amountMinorUnits }
            val mExpense = allTransactions.filter { it.transactionYearMonth == ymStr && it.isExpense }.sumOf { it.amountMinorUnits }
            val label = ym.month.name.take(3)
            MonthlyBarData(monthLabel = label, incomeMinorUnits = mIncome, expenseMinorUnits = mExpense)
        }
    }

    // Build trend line data from daily spending
    val trendPoints = remember(summary.dailyExpenses) {
        summary.dailyExpenses.entries.sortedBy { it.key }.map { (day, amt) ->
            TrendPoint(label = day.toString(), valueMinorUnits = amt)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("reports_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            MonthYearSelector(
                selectedYearMonth = selectedYm,
                onYearMonthChanged = { viewModel.setSelectedYearMonth(it) }
            )
        }

        // Export Actions Row (PDF Report & CSV)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.exportPdf(context) },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("export_pdf_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export PDF", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.exportCsv(context) },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("export_csv_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export CSV", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Rule-Based Insights Section
        if (summary.insights.isNotEmpty()) {
            item {
                Text(
                    text = "Key Financial Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    summary.insights.forEach { insight ->
                        InsightCard(insight = insight)
                    }
                }
            }
        }

        // Chart 1: Income vs Expenses Multi-Month Bar Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Income vs Expenses Comparison",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    IncomeExpenseBarChart(
                        data = barChartData,
                        currencyCode = profile.currencyCode
                    )
                }
            }
        }

        // Chart 2: Category Distribution Donut Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Category Spending Share",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    DonutChart(
                        items = summary.categoryBreakdown,
                        totalAmountMinorUnits = summary.totalExpenses,
                        currencyCode = profile.currencyCode
                    )
                }
            }
        }

        // Chart 3: Cash Flow Waterfall Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    WaterfallChart(
                        openingBalance = summary.openingBalance,
                        totalIncome = summary.totalIncome,
                        totalExpenses = summary.totalExpenses,
                        closingBalance = summary.availableBalance,
                        currencyCode = profile.currencyCode
                    )
                }
            }
        }

        // Chart 4: Daily Spending Calendar Heatmap
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CalendarHeatmap(
                        yearMonth = selectedYm,
                        dailyExpenses = summary.dailyExpenses,
                        currencyCode = profile.currencyCode
                    )
                }
            }
        }

        // Chart 5: Daily Trend Line Chart
        if (trendPoints.size >= 2) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Daily Expense Velocity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TrendLineChart(
                            points = trendPoints,
                            lineColor = Color(0xFFF43F5E),
                            gradientStartColor = Color(0x33F43F5E)
                        )
                    }
                }
            }
        }

        // Category Breakdown Ranking Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Detailed Category Rankings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (summary.categoryBreakdown.isEmpty()) {
                        Text(
                            text = "No category spending for this period.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        summary.categoryBreakdown.forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val catColor = try {
                                    Color(android.graphics.Color.parseColor(cat.colorHex))
                                } catch (_: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(catColor.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CategoryIconHelper.getIcon(cat.icon),
                                        contentDescription = null,
                                        tint = catColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = cat.categoryName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = FinancialEngine.formatMinorUnits(cat.totalMinorUnits, profile.currencyCode),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Progress bar of category share
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction = (cat.percentageOfTotal / 100.0).toFloat().coerceIn(0.01f, 1f))
                                                .height(6.dp)
                                                .background(catColor, RoundedCornerShape(3.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
