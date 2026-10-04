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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.calculation.FinancialEngine
import com.example.model.Transaction
import com.example.ui.FinoraViewModel
import com.example.ui.components.AddEditTransactionSheet
import com.example.ui.components.MonthYearSelector
import com.example.ui.components.TransactionListItem

@Composable
fun TransactionsScreen(
    viewModel: FinoraViewModel,
    modifier: Modifier = Modifier
) {
    val selectedYm by viewModel.selectedYearMonth.collectAsState()
    val filter by viewModel.transactionsFilter.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var showAddEditSheet by remember { mutableStateOf(false) }
    var selectedTxForEdit by remember { mutableStateOf<Transaction?>(null) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedTxForEdit = null
                    showAddEditSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_transaction_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("transactions_screen")
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Month Selector or "All Time" Toggle
            MonthYearSelector(
                selectedYearMonth = selectedYm,
                onYearMonthChanged = {
                    viewModel.setSelectedYearMonth(it)
                    if (filter.allMonths) viewModel.updateFilter { f -> f.copy(allMonths = false) }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = filter.searchQuery,
                onValueChange = { q -> viewModel.updateFilter { it.copy(searchQuery = q) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_search_input"),
                placeholder = { Text("Search by category, merchant, note, tags...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (filter.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.updateFilter { it.copy(searchQuery = "") } }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row (All, Income, Expense, Category, Sort)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Type Filter: All
                item {
                    FilterChip(
                        selected = filter.type == "all",
                        text = "All",
                        onClick = { viewModel.updateFilter { it.copy(type = "all") } },
                        tag = "filter_type_all"
                    )
                }
                // Type Filter: Income
                item {
                    FilterChip(
                        selected = filter.type == "income",
                        text = "Income",
                        selectedColor = Color(0xFF10B981),
                        onClick = { viewModel.updateFilter { it.copy(type = "income") } },
                        tag = "filter_type_income"
                    )
                }
                // Type Filter: Expenses
                item {
                    FilterChip(
                        selected = filter.type == "expense",
                        text = "Expenses",
                        selectedColor = Color(0xFFF43F5E),
                        onClick = { viewModel.updateFilter { it.copy(type = "expense") } },
                        tag = "filter_type_expense"
                    )
                }
                // Category Filter Dropdown
                item {
                    val activeCat = categories.firstOrNull { it.id == filter.categoryId }
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (filter.categoryId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            onClick = { showCategoryMenu = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = if (filter.categoryId != null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = activeCat?.name ?: "Category",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (filter.categoryId != null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Categories") },
                                onClick = {
                                    viewModel.updateFilter { it.copy(categoryId = null) }
                                    showCategoryMenu = false
                                }
                            )
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        viewModel.updateFilter { it.copy(categoryId = cat.id) }
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
                // Sort Toggle (Date vs Amount)
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (filter.sortByAmount) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { viewModel.updateFilter { it.copy(sortByAmount = !it.sortByAmount) } }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.Sort,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (filter.sortByAmount) "Sort: Amount" else "Sort: Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subheader: Count and Total Amount
            val totalInFiltered = transactions.sumOf { if (it.isIncome) it.amountMinorUnits else -it.amountMinorUnits }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${transactions.size} transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Net: ${FinancialEngine.formatMinorUnits(totalInFiltered, profile.currencyCode, hideAmount = profile.hideBalance)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (totalInFiltered >= 0) Color(0xFF10B981) else Color(0xFFF43F5E)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transactions List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No transactions found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try clearing filters or tap '+' below to record a new entry.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        val cat = categories.firstOrNull { it.id == tx.categoryId }
                        TransactionListItem(
                            transaction = tx,
                            categoryIcon = cat?.icon ?: "category",
                            categoryColorHex = cat?.colorHex ?: "#10B981",
                            hideAmount = profile.hideBalance,
                            onClick = {
                                selectedTxForEdit = tx
                                showAddEditSheet = true
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showAddEditSheet) {
        AddEditTransactionSheet(
            initialType = selectedTxForEdit?.type ?: "expense",
            existingTransaction = selectedTxForEdit,
            categories = categories,
            currencyCode = profile.currencyCode,
            onDismiss = {
                showAddEditSheet = false
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
fun FilterChip(
    selected: Boolean,
    text: String,
    onClick: () -> Unit,
    tag: String,
    selectedColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) selectedColor else MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick,
        modifier = Modifier.testTag(tag)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
