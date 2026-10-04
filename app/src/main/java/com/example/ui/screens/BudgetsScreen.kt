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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculation.FinancialEngine
import com.example.model.Budget
import com.example.model.Category
import com.example.ui.FinoraViewModel
import com.example.ui.components.CategoryIconHelper
import com.example.ui.components.MonthYearSelector

@Composable
fun BudgetsScreen(
    viewModel: FinoraViewModel,
    modifier: Modifier = Modifier
) {
    val selectedYm by viewModel.selectedYearMonth.collectAsState()
    val summary by viewModel.financialSummary.collectAsState()
    val budget by viewModel.currentBudget.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var showEditBudgetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("budgets_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            MonthYearSelector(
                selectedYearMonth = selectedYm,
                onYearMonthChanged = { viewModel.setSelectedYearMonth(it) }
            )
        }

        // Overall Budget Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MONTHLY BUDGET",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = if (budget != null && budget!!.overallLimitMinorUnits > 0L) {
                                    FinancialEngine.formatMinorUnits(budget!!.overallLimitMinorUnits, profile.currencyCode, includeDecimals = false)
                                } else "No budget set",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { showEditBudgetDialog = true },
                            modifier = Modifier.testTag("edit_budget_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Budget", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val limit = budget?.overallLimitMinorUnits ?: 0L
                    val spent = summary.totalExpenses
                    val percentUsed = if (limit > 0L) (spent.toDouble() / limit.toDouble()) * 100.0 else 0.0

                    val barColor = when {
                        percentUsed >= 100.0 -> Color(0xFFF43F5E) // Red
                        percentUsed >= (profile.budgetWarningPercent.toDouble()) -> Color(0xFFF59E0B) // Amber
                        else -> Color(0xFF10B981) // Emerald
                    }

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (percentUsed / 100.0).toFloat().coerceIn(0.01f, 1f))
                                .height(12.dp)
                                .background(barColor, RoundedCornerShape(6.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Spent vs Remaining row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Spent so far", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                FinancialEngine.formatMinorUnits(spent, profile.currencyCode, includeDecimals = false),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                if (limit > spent) "Remaining" else "Over budget by",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                FinancialEngine.formatMinorUnits(Math.abs(limit - spent), profile.currencyCode, includeDecimals = false),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = barColor
                            )
                        }
                    }

                    // Alert Banner if exceeded
                    if (percentUsed >= 100.0 && limit > 0L) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x22F43F5E),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF43F5E)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFF43F5E))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Alert: You have exceeded your monthly spending limit!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFF43F5E),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else if (percentUsed >= profile.budgetWarningPercent.toDouble() && limit > 0L) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x22F59E0B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Warning: Approaching spending limit (${percentUsed.toInt()}% consumed).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFF59E0B),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Actions Row (Copy Previous Month Budget, Set Savings Target)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.copyPreviousMonthBudget() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Last Month", fontSize = 12.sp)
                }

                Button(
                    onClick = { showEditBudgetDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Configure", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Category Budgets Header
        item {
            Text(
                text = "Category Budget Allocations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        val categoryLimits = budget?.categoryLimits ?: emptyMap()
        val expenseCategories = categories.filter { it.type == "expense" || it.type == "both" }

        if (categoryLimits.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No category limits assigned",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set category-specific spending targets to keep track of discretionary expenses.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(expenseCategories.filter { categoryLimits.containsKey(it.id) }) { cat ->
                val limitForCat = categoryLimits[cat.id] ?: 0L
                val spentInCat = summary.categoryBreakdown.firstOrNull { it.categoryId == cat.id }?.totalMinorUnits ?: 0L
                val catPercent = if (limitForCat > 0L) (spentInCat.toDouble() / limitForCat.toDouble()) * 100.0 else 0.0

                val catBarColor = when {
                    catPercent >= 100.0 -> Color(0xFFF43F5E)
                    catPercent >= (profile.budgetWarningPercent.toDouble()) -> Color(0xFFF59E0B)
                    else -> Color(0xFF10B981)
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(catBarColor.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CategoryIconHelper.getIcon(cat.icon),
                                        contentDescription = null,
                                        tint = catBarColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(cat.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${FinancialEngine.formatMinorUnits(spentInCat, profile.currencyCode)} of ${FinancialEngine.formatMinorUnits(limitForCat, profile.currencyCode)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = "${catPercent.toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = catBarColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = (catPercent / 100.0).toFloat().coerceIn(0.01f, 1f))
                                    .height(6.dp)
                                    .background(catBarColor, RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showEditBudgetDialog) {
        EditBudgetDialog(
            currentBudget = budget,
            categories = categories,
            currencyCode = profile.currencyCode,
            selectedMonth = selectedYm,
            onDismiss = { showEditBudgetDialog = false },
            onSave = { updatedBudget ->
                viewModel.saveBudget(updatedBudget)
                showEditBudgetDialog = false
            }
        )
    }
}

@Composable
fun EditBudgetDialog(
    currentBudget: Budget?,
    categories: List<Category>,
    currencyCode: String,
    selectedMonth: String,
    onDismiss: () -> Unit,
    onSave: (Budget) -> Unit
) {
    var overallLimitString by remember {
        mutableStateOf(
            if (currentBudget != null && currentBudget.overallLimitMinorUnits > 0L) {
                (currentBudget.overallLimitMinorUnits / 100L).toString()
            } else ""
        )
    }

    var savingsTargetString by remember {
        mutableStateOf(
            if (currentBudget != null && currentBudget.savingsTargetMinorUnits > 0L) {
                (currentBudget.savingsTargetMinorUnits / 100L).toString()
            } else ""
        )
    }

    // Category limits map
    val categoryLimitsState = remember {
        val map = mutableMapOf<String, String>()
        currentBudget?.categoryLimits?.forEach { (catId, amt) ->
            map[catId] = (amt / 100L).toString()
        }
        mutableStateOf(map)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Set Budget for $selectedMonth", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = overallLimitString,
                    onValueChange = { overallLimitString = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Overall Monthly Spending Limit ($currencyCode)") },
                    placeholder = { Text("e.g. 100000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_limit_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = savingsTargetString,
                    onValueChange = { savingsTargetString = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Monthly Savings Target ($currencyCode)") },
                    placeholder = { Text("e.g. 30000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text("Category Limits (Optional):", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))

                categories.filter { it.type == "expense" || it.type == "both" }.take(4).forEach { cat ->
                    val curVal = categoryLimitsState.value[cat.id] ?: ""
                    OutlinedTextField(
                        value = curVal,
                        onValueChange = { newVal ->
                            val updated = categoryLimitsState.value.toMutableMap()
                            if (newVal.isBlank()) updated.remove(cat.id) else updated[cat.id] = newVal.filter { it.isDigit() }
                            categoryLimitsState.value = updated
                        },
                        label = { Text(cat.name) },
                        placeholder = { Text("Limit in $currencyCode") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val overallMinor = FinancialEngine.parseToMinorUnits(overallLimitString)
                    val savingsMinor = FinancialEngine.parseToMinorUnits(savingsTargetString)
                    val catLimits = categoryLimitsState.value.mapValues {
                        FinancialEngine.parseToMinorUnits(it.value)
                    }.filterValues { it > 0L }

                    val newBudget = (currentBudget ?: Budget()).copy(
                        month = selectedMonth,
                        overallLimitMinorUnits = overallMinor,
                        savingsTargetMinorUnits = savingsMinor,
                        categoryLimits = catLimits
                    )
                    onSave(newBudget)
                },
                modifier = Modifier.testTag("save_budget_button")
            ) {
                Text("Save Budget", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
