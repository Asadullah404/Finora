package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculation.FinancialEngine
import com.example.model.FinancialInsight
import com.example.model.InsightType
import com.example.model.Transaction
import com.example.ui.FinoraViewModel
import com.example.ui.components.AddEditTransactionSheet
import com.example.ui.components.MainBalanceCard
import com.example.ui.components.MonthYearSelector
import com.example.ui.components.TransactionListItem
import com.example.ui.components.charts.DonutChart

@Composable
fun DashboardScreen(
    viewModel: FinoraViewModel,
    onNavigateToTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedYm by viewModel.selectedYearMonth.collectAsState()
    val summary by viewModel.financialSummary.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var addSheetInitialType by remember { mutableStateOf("expense") }
    var selectedTxForEdit by remember { mutableStateOf<Transaction?>(null) }

    val recentTransactions = remember(allTransactions, selectedYm) {
        allTransactions.filter { it.transactionYearMonth == selectedYm }.take(5)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Month Selector
            MonthYearSelector(
                selectedYearMonth = selectedYm,
                onYearMonthChanged = { viewModel.setSelectedYearMonth(it) }
            )
        }

        item {
            // Main Balance Card
            MainBalanceCard(
                summary = summary,
                currencyCode = profile.currencyCode,
                hideBalance = profile.hideBalance,
                onToggleHideBalance = { viewModel.toggleHideBalance() },
                onAddIncomeClick = {
                    addSheetInitialType = "income"
                    selectedTxForEdit = null
                    showAddSheet = true
                },
                onAddExpenseClick = {
                    addSheetInitialType = "expense"
                    selectedTxForEdit = null
                    showAddSheet = true
                }
            )
        }

        item {
            // KPI Summary Row (Savings Rate, Remaining Budget, Top Expense)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Savings Rate
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Savings Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format(java.util.Locale.US, "%.0f%%", summary.savingsRate),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Remaining Budget
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PieChart, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Budget Left", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (summary.overallBudget > 0L) FinancialEngine.formatMinorUnits(summary.budgetRemaining, profile.currencyCode, includeDecimals = false) else "No budget",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.budgetRemaining > 0L || summary.overallBudget == 0L) MaterialTheme.colorScheme.onSurface else Color(0xFFF43F5E)
                        )
                    }
                }
            }
        }

        // Rule-Based Insights Card
        if (summary.insights.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Monthly Financial Insights",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    summary.insights.take(2).forEach { insight ->
                        InsightCard(insight = insight)
                    }
                }
            }
        }

        // Spending by Category Donut Chart Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Spending Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = FinancialEngine.formatMinorUnits(summary.totalExpenses, profile.currencyCode, hideAmount = profile.hideBalance),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFF43F5E)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    DonutChart(
                        items = summary.categoryBreakdown,
                        totalAmountMinorUnits = summary.totalExpenses,
                        currencyCode = profile.currencyCode,
                        onCategoryClick = { /* Could filter */ }
                    )
                }
            }
        }

        // Recent Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(onClick = onNavigateToTransactions)
                        .padding(4.dp)
                )
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No transactions recorded for $selectedYm",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap '+ Income' or '- Expense' above to start logging records.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(recentTransactions, key = { it.id }) { tx ->
                val cat = categories.firstOrNull { it.id == tx.categoryId }
                TransactionListItem(
                    transaction = tx,
                    categoryIcon = cat?.icon ?: "category",
                    categoryColorHex = cat?.colorHex ?: "#10B981",
                    hideAmount = profile.hideBalance,
                    onClick = {
                        selectedTxForEdit = tx
                        showAddSheet = true
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showAddSheet) {
        AddEditTransactionSheet(
            initialType = addSheetInitialType,
            existingTransaction = selectedTxForEdit,
            categories = categories,
            currencyCode = profile.currencyCode,
            onDismiss = {
                showAddSheet = false
                selectedTxForEdit = null
            },
            onSave = { tx ->
                if (selectedTxForEdit == null) {
                    viewModel.addTransaction(tx)
                } else {
                    viewModel.updateTransaction(tx)
                }
            },
            onDelete = { txId ->
                viewModel.deleteTransaction(txId)
            }
        )
    }
}

@Composable
fun InsightCard(insight: FinancialInsight) {
    val (bg, border, tint) = when (insight.type) {
        InsightType.POSITIVE -> Triple(Color(0x1A10B981), Color(0x3310B981), Color(0xFF10B981))
        InsightType.WARNING -> Triple(Color(0x1AF59E0B), Color(0x33F59E0B), Color(0xFFF59E0B))
        InsightType.ALERT -> Triple(Color(0x1AF43F5E), Color(0x33F43F5E), Color(0xFFF43F5E))
        InsightType.INFO -> Triple(Color(0x1A06B6D4), Color(0x3306B6D4), Color(0xFF06B6D4))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (insight.type) {
                    InsightType.POSITIVE -> Icons.Default.TrendingUp
                    InsightType.WARNING -> Icons.Default.Warning
                    InsightType.ALERT -> Icons.Default.TrendingDown
                    InsightType.INFO -> Icons.Default.Info
                },
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = tint
                )
                Text(
                    text = insight.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
