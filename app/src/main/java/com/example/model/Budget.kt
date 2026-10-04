package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

data class Budget(
    val id: String = "",
    val userId: String = "",
    val month: String = "", // YYYY-MM
    val overallLimitMinorUnits: Long = 0L,
    val categoryLimits: Map<String, Long> = emptyMap(),
    val savingsTargetMinorUnits: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toFirestoreCreateMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "month" to month,
            "overallLimitMinorUnits" to overallLimitMinorUnits,
            "categoryLimits" to categoryLimits,
            "savingsTargetMinorUnits" to savingsTargetMinorUnits,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }
    }

    fun toFirestoreUpdateMap(): Map<String, Any?> {
        return mapOf(
            "month" to month,
            "overallLimitMinorUnits" to overallLimitMinorUnits,
            "categoryLimits" to categoryLimits,
            "savingsTargetMinorUnits" to savingsTargetMinorUnits,
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }
    }
}
