package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.PropertyName
import java.text.NumberFormat
import java.util.Locale

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val type: String = "expense", // "income" or "expense"
    val amountMinorUnits: Long = 0L,
    val currencyCode: String = "PKR",
    val categoryId: String = "",
    val categoryNameSnapshot: String = "",
    val transactionDate: String = "", // YYYY-MM-DD
    val transactionYearMonth: String = "", // YYYY-MM
    val description: String = "",
    val paymentMethod: String = "Cash",
    val merchant: String? = null,
    val tags: List<String> = emptyList(),
    @get:PropertyName("isRecurring")
    @set:PropertyName("isRecurring")
    var isRecurring: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val isIncome: Boolean
        get() = type.equals("income", ignoreCase = true)

    val isExpense: Boolean
        get() = type.equals("expense", ignoreCase = true)

    fun formattedAmount(hideAmount: Boolean = false): String {
        if (hideAmount) return "••••••"
        val majorUnits = amountMinorUnits.toDouble() / 100.0
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = if (amountMinorUnits % 100 == 0L) 0 else 2
            maximumFractionDigits = 2
        }
        val sign = if (isIncome) "+" else "-"
        return "$sign$currencyCode ${formatter.format(majorUnits)}"
    }

    fun toFirestoreCreateMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "id" to id,
            "userId" to userId,
            "type" to type,
            "amountMinorUnits" to amountMinorUnits,
            "currencyCode" to currencyCode,
            "categoryId" to categoryId,
            "categoryNameSnapshot" to categoryNameSnapshot,
            "transactionDate" to transactionDate,
            "transactionYearMonth" to transactionYearMonth,
            "description" to description,
            "paymentMethod" to paymentMethod,
            "tags" to tags,
            "isRecurring" to isRecurring,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!merchant.isNullOrBlank()) {
            map["merchant"] = merchant
        }
        return map.filterValues { it != null }
    }

    fun toFirestoreUpdateMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "type" to type,
            "amountMinorUnits" to amountMinorUnits,
            "currencyCode" to currencyCode,
            "categoryId" to categoryId,
            "categoryNameSnapshot" to categoryNameSnapshot,
            "transactionDate" to transactionDate,
            "transactionYearMonth" to transactionYearMonth,
            "description" to description,
            "paymentMethod" to paymentMethod,
            "tags" to tags,
            "isRecurring" to isRecurring,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!merchant.isNullOrBlank()) {
            map["merchant"] = merchant
        }
        return map.filterValues { it != null }
    }
}
