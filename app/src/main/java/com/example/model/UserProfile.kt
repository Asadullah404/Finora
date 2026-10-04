package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.PropertyName

data class UserProfile(
    val userId: String = "",
    val currencyCode: String = "PKR",
    val openingBalanceMinorUnits: Long = 0L,
    val firstDayOfWeek: Int = 1, // 1 = Sunday, 2 = Monday
    @get:PropertyName("hideBalance")
    @set:PropertyName("hideBalance")
    var hideBalance: Boolean = false,
    val budgetWarningPercent: Int = 80,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toFirestoreCreateMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "currencyCode" to currencyCode,
            "openingBalanceMinorUnits" to openingBalanceMinorUnits,
            "firstDayOfWeek" to firstDayOfWeek,
            "hideBalance" to hideBalance,
            "budgetWarningPercent" to budgetWarningPercent,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }
    }

    fun toFirestoreUpdateMap(): Map<String, Any?> {
        return mapOf(
            "currencyCode" to currencyCode,
            "openingBalanceMinorUnits" to openingBalanceMinorUnits,
            "firstDayOfWeek" to firstDayOfWeek,
            "hideBalance" to hideBalance,
            "budgetWarningPercent" to budgetWarningPercent,
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }
    }
}
