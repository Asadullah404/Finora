package com.example.calculation

import com.example.model.Budget
import com.example.model.Category
import com.example.model.CategorySpending
import com.example.model.FinancialInsight
import com.example.model.FinancialSummary
import com.example.model.InsightType
import com.example.model.Transaction
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object FinancialEngine {

    /**
     * Converts a user-entered amount string into integer minor units (e.g. 100 -> 10000 paisa/cents).
     */
    fun parseToMinorUnits(input: String): Long {
        if (input.isBlank()) return 0L
        val clean = input.trim().replace(",", "")
        return try {
            if (clean.contains(".")) {
                val parts = clean.split(".")
                val major = parts[0].toLongOrNull() ?: 0L
                val minorStr = parts.getOrElse(1) { "0" }.padEnd(2, '0').take(2)
                val minor = minorStr.toLongOrNull() ?: 0L
                (major * 100L) + minor
            } else {
                (clean.toLongOrNull() ?: 0L) * 100L
            }
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Formats integer minor units to user-friendly currency string (e.g. 10000 -> "PKR 100.00").
     */
    fun formatMinorUnits(
        minorUnits: Long,
        currencyCode: String = "PKR",
        hideAmount: Boolean = false,
        includeDecimals: Boolean = true
    ): String {
        if (hideAmount) return "••••••"
        val isNegative = minorUnits < 0
        val absUnits = Math.abs(minorUnits)
        val major = absUnits / 100L
        val minor = absUnits % 100L

        val formatter = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))
        val formattedMajor = formatter.format(major)
        val result = if (includeDecimals && minor != 0L) {
            String.format(Locale.US, "%s %s.%02d", currencyCode, formattedMajor, minor)
        } else if (includeDecimals) {
            String.format(Locale.US, "%s %s.00", currencyCode, formattedMajor)
        } else {
            String.format(Locale.US, "%s %s", currencyCode, formattedMajor)
        }
        return if (isNegative) "-$result" else result
    }

    /**
     * Calculates the previous calendar month string (e.g. "2026-10" -> "2026-09").
     */
    fun getPreviousMonth(yearMonth: String): String {
        return try {
            val ym = YearMonth.parse(yearMonth)
            ym.minusMonths(1).toString()
        } catch (_: Exception) {
            yearMonth
        }
    }

    /**
     * Calculates percentage change between current and previous value.
     * Returns null if previous is 0.
     */
    fun calculatePercentageChange(current: Long, previous: Long): Double? {
        if (previous == 0L) return null
        val diff = (current - previous).toDouble()
        return (diff / previous.toDouble()) * 100.0
    }

    /**
     * Centralized computation of FinancialSummary for a selected month.
     */
    fun computeSummary(
        selectedYearMonth: String,
        allTransactions: List<Transaction>,
        categories: List<Category>,
        budget: Budget?,
        openingBalance: Long = 0L
    ): FinancialSummary {
        val categoryMap = categories.associateBy { it.id }

        // Filter transactions up to the end of selected month for cumulative balance
        val selectedYm = try {
            YearMonth.parse(selectedYearMonth)
        } catch (_: Exception) {
            YearMonth.now()
        }

        var cumulativeIncome = 0L
        var cumulativeExpenses = 0L
        var monthlyIncome = 0L
        var monthlyExpenses = 0L
        val categoryTotals = mutableMapOf<String, Long>()
        val dailyMap = mutableMapOf<Int, Long>()

        val prevYmString = getPreviousMonth(selectedYearMonth)
        var prevMonthlyIncome = 0L
        var prevMonthlyExpenses = 0L

        for (tx in allTransactions) {
            val txYm = tx.transactionYearMonth.ifBlank {
                try {
                    tx.transactionDate.take(7)
                } catch (_: Exception) {
                    ""
                }
            }

            // Cumulative balance calculation includes all transactions up to current/selected period
            val txDateObj = try {
                LocalDate.parse(tx.transactionDate, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (_: Exception) {
                null
            }

            val isBeforeOrInSelectedMonth = if (txDateObj != null) {
                !YearMonth.from(txDateObj).isAfter(selectedYm)
            } else {
                txYm <= selectedYearMonth
            }

            if (isBeforeOrInSelectedMonth) {
                if (tx.isIncome) cumulativeIncome += tx.amountMinorUnits
                if (tx.isExpense) cumulativeExpenses += tx.amountMinorUnits
            }

            // Selected Month Totals
            if (txYm == selectedYearMonth) {
                if (tx.isIncome) {
                    monthlyIncome += tx.amountMinorUnits
                } else if (tx.isExpense) {
                    monthlyExpenses += tx.amountMinorUnits
                    categoryTotals[tx.categoryId] = (categoryTotals[tx.categoryId] ?: 0L) + tx.amountMinorUnits

                    if (txDateObj != null) {
                        val day = txDateObj.dayOfMonth
                        dailyMap[day] = (dailyMap[day] ?: 0L) + tx.amountMinorUnits
                    }
                }
            }

            // Previous Month Totals
            if (txYm == prevYmString) {
                if (tx.isIncome) prevMonthlyIncome += tx.amountMinorUnits
                if (tx.isExpense) prevMonthlyExpenses += tx.amountMinorUnits
            }
        }

        val netCashFlow = monthlyIncome - monthlyExpenses
        val availableBalance = openingBalance + cumulativeIncome - cumulativeExpenses

        val savingsRate = if (monthlyIncome > 0L) {
            val rate = ((monthlyIncome - monthlyExpenses).toDouble() / monthlyIncome.toDouble()) * 100.0
            Math.max(-100.0, Math.min(100.0, rate))
        } else {
            0.0
        }

        val incomeChangePercent = calculatePercentageChange(monthlyIncome, prevMonthlyIncome)
        val expenseChangePercent = calculatePercentageChange(monthlyExpenses, prevMonthlyExpenses)

        // Build category breakdown
        val breakdown = categoryTotals.map { (catId, total) ->
            val cat = categoryMap[catId]
            val pct = if (monthlyExpenses > 0L) (total.toDouble() / monthlyExpenses.toDouble()) * 100.0 else 0.0
            CategorySpending(
                categoryId = catId,
                categoryName = cat?.name ?: (allTransactions.firstOrNull { it.categoryId == catId }?.categoryNameSnapshot ?: "Uncategorized"),
                icon = cat?.icon ?: "category",
                colorHex = cat?.colorHex ?: "#94A3B8",
                totalMinorUnits = total,
                percentageOfTotal = pct
            )
        }.sortedByDescending { it.totalMinorUnits }

        val largestCategory = breakdown.firstOrNull()

        // Budget Calculations
        val overallBudget = budget?.overallLimitMinorUnits ?: 0L
        val budgetSpent = monthlyExpenses
        val budgetRemaining = Math.max(0L, overallBudget - budgetSpent)
        val budgetUsedPercent = if (overallBudget > 0L) {
            (budgetSpent.toDouble() / overallBudget.toDouble()) * 100.0
        } else 0.0

        // Rule-Based Insights
        val insights = generateInsights(
            selectedYearMonth = selectedYearMonth,
            monthlyIncome = monthlyIncome,
            monthlyExpenses = monthlyExpenses,
            netCashFlow = netCashFlow,
            savingsRate = savingsRate,
            expenseChangePercent = expenseChangePercent,
            largestCategory = largestCategory,
            dailyMap = dailyMap,
            overallBudget = overallBudget,
            budgetSpent = budgetSpent,
            currencyCode = allTransactions.firstOrNull()?.currencyCode ?: "PKR"
        )

        return FinancialSummary(
            yearMonth = selectedYearMonth,
            openingBalance = openingBalance,
            totalIncome = monthlyIncome,
            totalExpenses = monthlyExpenses,
            netCashFlow = netCashFlow,
            availableBalance = availableBalance,
            savingsRate = savingsRate,
            previousMonthIncome = prevMonthlyIncome,
            previousMonthExpenses = prevMonthlyExpenses,
            incomeChangePercent = incomeChangePercent,
            expenseChangePercent = expenseChangePercent,
            largestCategory = largestCategory,
            categoryBreakdown = breakdown,
            dailyExpenses = dailyMap,
            insights = insights,
            overallBudget = overallBudget,
            budgetSpent = budgetSpent,
            budgetRemaining = budgetRemaining,
            budgetUsedPercent = budgetUsedPercent
        )
    }

    private fun generateInsights(
        selectedYearMonth: String,
        monthlyIncome: Long,
        monthlyExpenses: Long,
        netCashFlow: Long,
        savingsRate: Double,
        expenseChangePercent: Double?,
        largestCategory: CategorySpending?,
        dailyMap: Map<Int, Long>,
        overallBudget: Long,
        budgetSpent: Long,
        currencyCode: String
    ): List<FinancialInsight> {
        val list = mutableListOf<FinancialInsight>()

        if (monthlyIncome > 0L && savingsRate > 20.0) {
            list.add(
                FinancialInsight(
                    id = "high_savings",
                    title = "Healthy Savings Rate",
                    message = String.format(Locale.US, "You have saved %.1f%% of your income this month. Great financial discipline!", savingsRate),
                    type = InsightType.POSITIVE,
                    icon = "savings"
                )
            )
        } else if (monthlyIncome > 0L && netCashFlow < 0L) {
            list.add(
                FinancialInsight(
                    id = "negative_cashflow",
                    title = "Cash Flow Warning",
                    message = "Your expenses exceed your confirmed income this month. Review your non-essential spending.",
                    type = InsightType.ALERT,
                    icon = "warning"
                )
            )
        }

        if (expenseChangePercent != null) {
            if (expenseChangePercent < -5.0) {
                list.add(
                    FinancialInsight(
                        id = "lower_expenses",
                        title = "Spending Decreased",
                        message = String.format(Locale.US, "You spent %.1f%% less than the previous month.", Math.abs(expenseChangePercent)),
                        type = InsightType.POSITIVE,
                        icon = "trending_down"
                    )
                )
            } else if (expenseChangePercent > 10.0) {
                list.add(
                    FinancialInsight(
                        id = "higher_expenses",
                        title = "Spending Increased",
                        message = String.format(Locale.US, "Your total expenses increased by %.1f%% compared to last month.", expenseChangePercent),
                        type = InsightType.WARNING,
                        icon = "trending_up"
                    )
                )
            }
        }

        if (largestCategory != null && largestCategory.percentageOfTotal > 25.0) {
            list.add(
                FinancialInsight(
                    id = "largest_category",
                    title = "Major Spending Area",
                    message = String.format(
                        Locale.US,
                        "%s accounts for %.1f%% of your total spending (%s).",
                        largestCategory.categoryName,
                        largestCategory.percentageOfTotal,
                        formatMinorUnits(largestCategory.totalMinorUnits, currencyCode)
                    ),
                    type = InsightType.INFO,
                    icon = "pie_chart"
                )
            )
        }

        if (dailyMap.isNotEmpty()) {
            val highestDay = dailyMap.maxByOrNull { it.value }
            if (highestDay != null && highestDay.value > 0L) {
                list.add(
                    FinancialInsight(
                        id = "peak_day",
                        title = "Highest Spending Day",
                        message = String.format(
                            Locale.US,
                            "Your highest-spending day was day %d with %s spent.",
                            highestDay.key,
                            formatMinorUnits(highestDay.value, currencyCode)
                        ),
                        type = InsightType.INFO,
                        icon = "calendar_today"
                    )
                )
            }
        }

        if (overallBudget > 0L) {
            val remaining = overallBudget - budgetSpent
            if (remaining < 0L) {
                list.add(
                    FinancialInsight(
                        id = "budget_exceeded",
                        title = "Budget Exceeded",
                        message = String.format(
                            Locale.US,
                            "You have exceeded your monthly budget by %s.",
                            formatMinorUnits(Math.abs(remaining), currencyCode)
                        ),
                        type = InsightType.ALERT,
                        icon = "error"
                    )
                )
            } else if (budgetSpent >= (overallBudget * 0.85)) {
                list.add(
                    FinancialInsight(
                        id = "budget_warning",
                        title = "Approaching Budget Limit",
                        message = String.format(
                            Locale.US,
                            "You have consumed %.0f%% of your budget limit.",
                            (budgetSpent.toDouble() / overallBudget.toDouble()) * 100.0
                        ),
                        type = InsightType.WARNING,
                        icon = "notifications_active"
                    )
                )
            }
        }

        return list
    }
}
