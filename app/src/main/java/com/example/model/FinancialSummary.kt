package com.example.model

data class CategorySpending(
    val categoryId: String,
    val categoryName: String,
    val icon: String,
    val colorHex: String,
    val totalMinorUnits: Long,
    val percentageOfTotal: Double
)

data class FinancialInsight(
    val id: String,
    val title: String,
    val message: String,
    val type: InsightType,
    val icon: String
)

enum class InsightType {
    POSITIVE, WARNING, INFO, ALERT
}

data class FinancialSummary(
    val yearMonth: String,
    val openingBalance: Long = 0L,
    val totalIncome: Long = 0L,
    val totalExpenses: Long = 0L,
    val netCashFlow: Long = 0L,
    val availableBalance: Long = 0L,
    val savingsRate: Double = 0.0,
    val previousMonthIncome: Long = 0L,
    val previousMonthExpenses: Long = 0L,
    val incomeChangePercent: Double? = null,
    val expenseChangePercent: Double? = null,
    val largestCategory: CategorySpending? = null,
    val categoryBreakdown: List<CategorySpending> = emptyList(),
    val dailyExpenses: Map<Int, Long> = emptyMap(), // Day of month (1..31) -> amountMinorUnits
    val insights: List<FinancialInsight> = emptyList(),
    val overallBudget: Long = 0L,
    val budgetSpent: Long = 0L,
    val budgetRemaining: Long = 0L,
    val budgetUsedPercent: Double = 0.0
)
