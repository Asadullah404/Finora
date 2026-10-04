package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Transaction

@Composable
fun TransactionListItem(
    transaction: Transaction,
    categoryIcon: String = "category",
    categoryColorHex: String = "#10B981",
    hideAmount: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val catColor = try {
        Color(android.graphics.Color.parseColor(categoryColorHex))
    } catch (_: Exception) {
        if (transaction.isIncome) Color(0xFF10B981) else Color(0xFFF43F5E)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_item_${transaction.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(catColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CategoryIconHelper.getIcon(categoryIcon),
                    contentDescription = transaction.categoryNameSnapshot,
                    tint = catColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Transaction Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.categoryNameSnapshot.ifBlank { "Transaction" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (transaction.isRecurring) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "Recurring",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                val subtext = buildString {
                    append(transaction.transactionDate)
                    if (transaction.paymentMethod.isNotBlank()) {
                        append(" • ")
                        append(transaction.paymentMethod)
                    }
                    if (!transaction.merchant.isNullOrBlank()) {
                        append(" • ")
                        append(transaction.merchant)
                    }
                }

                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (transaction.description.isNotBlank()) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount
            val amountColor = if (transaction.isIncome) Color(0xFF10B981) else Color(0xFFF43F5E)
            Text(
                text = transaction.formattedAmount(hideAmount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}
