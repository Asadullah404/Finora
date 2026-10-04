package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.PropertyName

data class Category(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val icon: String = "category",
    val colorHex: String = "#10B981",
    val type: String = "expense", // "income", "expense", or "both"
    @get:PropertyName("isDefault")
    @set:PropertyName("isDefault")
    var isDefault: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toFirestoreCreateMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "name" to name,
            "icon" to icon,
            "colorHex" to colorHex,
            "type" to type,
            "isDefault" to isDefault,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }
    }

    fun toFirestoreUpdateMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "icon" to icon,
            "colorHex" to colorHex,
            "type" to type,
            "isDefault" to isDefault,
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }
    }

    companion object {
        fun defaultCategories(userId: String): List<Category> = listOf(
            Category("cat_salary", userId, "Salary", "payments", "#10B981", "income", true),
            Category("cat_bonus", userId, "Bonus", "military_tech", "#F59E0B", "income", true),
            Category("cat_freelance", userId, "Freelance", "laptop_mac", "#3B82F6", "income", true),
            Category("cat_business", userId, "Business", "store", "#8B5CF6", "income", true),
            Category("cat_refund", userId, "Refund / Adjustment", "currency_exchange", "#06B6D4", "income", true),
            Category("cat_food", userId, "Food & Groceries", "restaurant", "#10B981", "expense", true),
            Category("cat_transport", userId, "Transport & Fuel", "directions_car", "#3B82F6", "expense", true),
            Category("cat_vehicle", userId, "Bike / Vehicle Installments", "two_wheeler", "#6366F1", "expense", true),
            Category("cat_family", userId, "Family Support", "family_restroom", "#EC4899", "expense", true),
            Category("cat_housing", userId, "Rent & Housing", "home", "#8B5CF6", "expense", true),
            Category("cat_bills", userId, "Utilities & Bills", "bolt", "#F59E0B", "expense", true),
            Category("cat_shopping", userId, "Shopping", "shopping_bag", "#14B8A6", "expense", true),
            Category("cat_healthcare", userId, "Healthcare", "medical_services", "#EF4444", "expense", true),
            Category("cat_education", userId, "Education", "school", "#06B6D4", "expense", true),
            Category("cat_entertainment", userId, "Entertainment", "sports_esports", "#A855F7", "expense", true),
            Category("cat_subscriptions", userId, "Subscriptions", "subscriptions", "#64748B", "expense", true),
            Category("cat_personal_care", userId, "Personal Care", "spa", "#F43F5E", "expense", true),
            Category("cat_travel", userId, "Travel", "flight", "#0EA5E9", "expense", true),
            Category("cat_other", userId, "Other", "more_horiz", "#94A3B8", "both", true)
        )
    }
}
