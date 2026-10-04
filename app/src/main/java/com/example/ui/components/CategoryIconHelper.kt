package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
    fun getIcon(key: String): ImageVector {
        return when (key.lowercase()) {
            "payments", "salary", "cash" -> Icons.Default.Payments
            "restaurant", "food", "grocery" -> Icons.Default.Restaurant
            "directions_car", "car", "fuel", "transport" -> Icons.Default.DirectionsCar
            "two_wheeler", "bike" -> Icons.Default.TwoWheeler
            "family_restroom", "family" -> Icons.Default.FamilyRestroom
            "home", "housing", "rent" -> Icons.Default.Home
            "bolt", "bills", "utilities" -> Icons.Default.Bolt
            "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
            "medical_services", "health" -> Icons.Default.MedicalServices
            "school", "education" -> Icons.Default.School
            "sports_esports", "entertainment" -> Icons.Default.SportsEsports
            "subscriptions", "streaming" -> Icons.Default.Subscriptions
            "spa", "personal_care" -> Icons.Default.Spa
            "flight", "travel" -> Icons.Default.Flight
            "military_tech", "bonus" -> Icons.Default.MilitaryTech
            "laptop_mac", "freelance" -> Icons.Default.LaptopMac
            "store", "business" -> Icons.Default.Store
            "currency_exchange", "refund" -> Icons.Default.CurrencyExchange
            "savings" -> Icons.Default.Savings
            "trending_up" -> Icons.AutoMirrored.Filled.TrendingUp
            else -> Icons.Default.Category
        }
    }
}
