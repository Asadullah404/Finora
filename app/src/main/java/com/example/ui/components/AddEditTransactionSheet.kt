package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculation.FinancialEngine
import com.example.model.Category
import com.example.model.Transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionSheet(
    initialType: String = "expense",
    existingTransaction: Transaction? = null,
    categories: List<Category>,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var type by remember { mutableStateOf(existingTransaction?.type ?: initialType) }
    var amountString by remember {
        mutableStateOf(
            if (existingTransaction != null) {
                val major = existingTransaction.amountMinorUnits.toDouble() / 100.0
                if (existingTransaction.amountMinorUnits % 100 == 0L) {
                    (existingTransaction.amountMinorUnits / 100L).toString()
                } else {
                    String.format(java.util.Locale.US, "%.2f", major)
                }
            } else ""
        )
    }

    val availableCategories = categories.filter {
        it.type == "both" || it.type == type
    }

    var selectedCategoryId by remember {
        mutableStateOf(
            existingTransaction?.categoryId
                ?: availableCategories.firstOrNull()?.id
                ?: "cat_other"
        )
    }

    var transactionDate by remember {
        mutableStateOf(existingTransaction?.transactionDate ?: LocalDate.now().toString())
    }

    var description by remember { mutableStateOf(existingTransaction?.description ?: "") }
    var paymentMethod by remember { mutableStateOf(existingTransaction?.paymentMethod ?: "Cash") }
    var merchant by remember { mutableStateOf(existingTransaction?.merchant ?: "") }
    var tagsString by remember { mutableStateOf(existingTransaction?.tags?.joinToString(", ") ?: "") }
    var isRecurring by remember { mutableStateOf(existingTransaction?.isRecurring ?: false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val paymentMethods = listOf("Cash", "Bank Transfer", "Credit Card", "Debit Card", "EasyPaisa", "JazzCash", "Nayapay", "SadaPay", "Other")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingTransaction == null) "New Transaction" else "Edit Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Income / Expense Segmented Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                val isIncome = type == "income"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isIncome) Color(0xFF10B981) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            type = "income"
                            val firstIncomeCat = categories.firstOrNull { it.type == "income" || it.type == "both" }
                            if (firstIncomeCat != null) selectedCategoryId = firstIncomeCat.id
                        }
                        .padding(vertical = 10.dp)
                        .testTag("tab_income"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Income (+)",
                        fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                        color = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val isExpense = type == "expense"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isExpense) Color(0xFFF43F5E) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            type = "expense"
                            val firstExpenseCat = categories.firstOrNull { it.type == "expense" || it.type == "both" }
                            if (firstExpenseCat != null) selectedCategoryId = firstExpenseCat.id
                        }
                        .padding(vertical = 10.dp)
                        .testTag("tab_expense"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Expense (-)",
                        fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Medium,
                        color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Amount Input Field
            OutlinedTextField(
                value = amountString,
                onValueChange = { input ->
                    if (input.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                        amountString = input
                        errorMessage = null
                    }
                },
                label = { Text("Amount ($currencyCode)") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_amount_input"),
                shape = RoundedCornerShape(12.dp)
            )

            if (amountString.isNotBlank()) {
                val parsed = FinancialEngine.parseToMinorUnits(amountString)
                Text(
                    text = "Entered: ${FinancialEngine.formatMinorUnits(parsed, currencyCode)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selector Grid
            Text(
                text = "Select Category",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCategories.forEach { cat ->
                    val isSelected = cat.id == selectedCategoryId
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(cat.colorHex))
                    } catch (_: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) catColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) catColor else Color.Transparent
                        ),
                        onClick = { selectedCategoryId = cat.id },
                        modifier = Modifier.testTag("category_chip_${cat.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(catColor.copy(alpha = 0.25f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CategoryIconHelper.getIcon(cat.icon),
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Picker Row
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                onClick = {
                    val cal = Calendar.getInstance()
                    try {
                        val parsed = LocalDate.parse(transactionDate)
                        cal.set(parsed.year, parsed.monthValue - 1, parsed.dayOfMonth)
                    } catch (_: Exception) {}

                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            transactionDate = String.format(java.util.Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("date_picker_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Transaction Date", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(transactionDate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method Chips
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                paymentMethods.forEach { method ->
                    val isSelected = method == paymentMethod
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { paymentMethod = method }
                    ) {
                        Text(
                            text = method,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Merchant / Recipient & Description
            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant / Payee (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Notes (Optional)") },
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = tagsString,
                onValueChange = { tagsString = it },
                label = { Text("Tags (Optional, comma-separated)") },
                placeholder = { Text("e.g. office, trip, family") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recurring Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Recurring Transaction", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("Mark as monthly recurring item", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save & Delete Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (existingTransaction != null && onDelete != null) {
                    OutlinedButton(
                        onClick = {
                            onDelete(existingTransaction.id)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(0.4f)
                            .height(50.dp)
                            .testTag("delete_transaction_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF43F5E))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }

                Button(
                    onClick = {
                        val minorUnits = FinancialEngine.parseToMinorUnits(amountString)
                        if (minorUnits <= 0L) {
                            errorMessage = "Please enter a valid amount greater than 0."
                            return@Button
                        }

                        val selectedCat = categories.firstOrNull { it.id == selectedCategoryId }
                        val catName = selectedCat?.name ?: "General"
                        val yearMonth = try {
                            transactionDate.take(7)
                        } catch (_: Exception) {
                            LocalDate.now().toString().take(7)
                        }

                        val tags = tagsString.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .take(10)

                        val newTx = (existingTransaction ?: Transaction()).copy(
                            type = type,
                            amountMinorUnits = minorUnits,
                            currencyCode = currencyCode,
                            categoryId = selectedCategoryId,
                            categoryNameSnapshot = catName,
                            transactionDate = transactionDate,
                            transactionYearMonth = yearMonth,
                            description = description.trim(),
                            paymentMethod = paymentMethod,
                            merchant = merchant.trim().ifBlank { null },
                            tags = tags,
                            isRecurring = isRecurring
                        )
                        onSave(newTx)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("save_transaction_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == "income") Color(0xFF10B981) else Color(0xFFF43F5E)
                    )
                ) {
                    Text(
                        text = if (existingTransaction == null) "Save Transaction" else "Update Transaction",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
