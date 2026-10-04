package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculation.FinancialEngine
import com.example.model.FinancialSummary

@Composable
fun MainBalanceCard(
    summary: FinancialSummary,
    currencyCode: String,
    hideBalance: Boolean,
    onToggleHideBalance: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("main_balance_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF14203D),
                            Color(0xFF0B132B)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header: Title & Eye Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AVAILABLE BALANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Cumulative across all accounts",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }

                    IconButton(
                        onClick = onToggleHideBalance,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("toggle_balance_visibility")
                    ) {
                        Icon(
                            imageVector = if (hideBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (hideBalance) "Show Balance" else "Hide Balance",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Big Balance Display
                Text(
                    text = FinancialEngine.formatMinorUnits(summary.availableBalance, currencyCode, hideAmount = hideBalance),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.testTag("available_balance_text")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Monthly Income vs Expense Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Income Chip
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x1A10B981)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0x3310B981), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Monthly Income",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "+${FinancialEngine.formatMinorUnits(summary.totalIncome, currencyCode, hideAmount = hideBalance, includeDecimals = false)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }

                    // Expense Chip
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x1AF43F5E)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0x33F43F5E), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = Color(0xFFF43F5E),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Monthly Expenses",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "-${FinancialEngine.formatMinorUnits(summary.totalExpenses, currencyCode, hideAmount = hideBalance, includeDecimals = false)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF43F5E)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Net Cash Flow and Savings Rate Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isPositiveFlow = summary.netCashFlow >= 0
                    val flowColor = if (isPositiveFlow) Color(0xFF10B981) else Color(0xFFF43F5E)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Net Cash Flow: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = (if (isPositiveFlow) "+" else "") + FinancialEngine.formatMinorUnits(summary.netCashFlow, currencyCode, hideAmount = hideBalance, includeDecimals = false),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = flowColor
                        )
                    }

                    Text(
                        text = String.format(java.util.Locale.US, "Savings Rate: %.0f%%", summary.savingsRate),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF38BDF8)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Action Buttons (+ Income, - Expense)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAddIncomeClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("quick_add_income_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Income", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = onAddExpenseClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("quick_add_expense_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Expense", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
