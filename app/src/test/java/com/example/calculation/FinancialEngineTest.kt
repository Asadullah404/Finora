package com.example.calculation

import com.example.model.Budget
import com.example.model.Category
import com.example.model.Transaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialEngineTest {

    @Test
    fun parseToMinorUnits_correctlyParsesInputs() {
        assertEquals(10000L, FinancialEngine.parseToMinorUnits("100"))
        assertEquals(10050L, FinancialEngine.parseToMinorUnits("100.5"))
        assertEquals(10050L, FinancialEngine.parseToMinorUnits("100.50"))
        assertEquals(10000000L, FinancialEngine.parseToMinorUnits("100,000"))
        assertEquals(0L, FinancialEngine.parseToMinorUnits(""))
        assertEquals(0L, FinancialEngine.parseToMinorUnits("abc"))
    }

    @Test
    fun formatMinorUnits_formatsPKRAndOtherCurrenciesCorrectly() {
        assertEquals("PKR 100.00", FinancialEngine.formatMinorUnits(10000L, "PKR"))
        assertEquals("PKR 1,500.50", FinancialEngine.formatMinorUnits(150050L, "PKR"))
        assertEquals("-PKR 250.00", FinancialEngine.formatMinorUnits(-25000L, "PKR"))
        assertEquals("••••••", FinancialEngine.formatMinorUnits(10000L, "PKR", hideAmount = true))
    }

    @Test
    fun previousMonth_handlesYearAndMonthBoundaries() {
        assertEquals("2026-09", FinancialEngine.getPreviousMonth("2026-10"))
        assertEquals("2025-12", FinancialEngine.getPreviousMonth("2026-01"))
    }

    @Test
    fun computeSummary_withSalaryAndExpenses_calculatesCorrectTotalsAndSavingsRate() {
        val transactions = listOf(
            Transaction(
                id = "tx1",
                userId = "u1",
                type = "income",
                amountMinorUnits = 20000000L, // 200,000 PKR
                categoryId = "cat_salary",
                categoryNameSnapshot = "Salary",
                transactionDate = "2026-10-01",
                transactionYearMonth = "2026-10"
            ),
            Transaction(
                id = "tx2",
                userId = "u1",
                type = "expense",
                amountMinorUnits = 5000000L, // 50,000 PKR
                categoryId = "cat_food",
                categoryNameSnapshot = "Food & Groceries",
                transactionDate = "2026-10-05",
                transactionYearMonth = "2026-10"
            ),
            Transaction(
                id = "tx3",
                userId = "u1",
                type = "expense",
                amountMinorUnits = 3000000L, // 30,000 PKR
                categoryId = "cat_rent",
                categoryNameSnapshot = "Rent",
                transactionDate = "2026-10-10",
                transactionYearMonth = "2026-10"
            )
        )

        val categories = listOf(
            Category("cat_salary", "u1", "Salary", "payments", "#10B981", "income"),
            Category("cat_food", "u1", "Food & Groceries", "restaurant", "#10B981", "expense"),
            Category("cat_rent", "u1", "Rent", "home", "#8B5CF6", "expense")
        )

        val budget = Budget(
            id = "b1",
            userId = "u1",
            month = "2026-10",
            overallLimitMinorUnits = 10000000L // 100,000 PKR
        )

        val summary = FinancialEngine.computeSummary(
            selectedYearMonth = "2026-10",
            allTransactions = transactions,
            categories = categories,
            budget = budget,
            openingBalance = 5000000L // 50,000 PKR
        )

        assertEquals(20000000L, summary.totalIncome)
        assertEquals(8000000L, summary.totalExpenses)
        assertEquals(12000000L, summary.netCashFlow)
        assertEquals(17000000L, summary.availableBalance)
        assertEquals(60.0, summary.savingsRate, 0.01)
        assertEquals(8000000L, summary.budgetSpent)
        assertEquals(2000000L, summary.budgetRemaining)
        assertEquals(80.0, summary.budgetUsedPercent, 0.01)
        assertEquals(2, summary.categoryBreakdown.size)
        assertEquals("Food & Groceries", summary.largestCategory?.categoryName)
    }

    @Test
    fun computeSummary_withZeroIncome_savingsRateIsZero() {
        val transactions = listOf(
            Transaction(
                id = "tx1",
                userId = "u1",
                type = "expense",
                amountMinorUnits = 1000000L,
                categoryId = "cat_food",
                categoryNameSnapshot = "Food",
                transactionDate = "2026-10-02",
                transactionYearMonth = "2026-10"
            )
        )

        val summary = FinancialEngine.computeSummary(
            selectedYearMonth = "2026-10",
            allTransactions = transactions,
            categories = emptyList(),
            budget = null,
            openingBalance = 2000000L
        )

        assertEquals(0L, summary.totalIncome)
        assertEquals(1000000L, summary.totalExpenses)
        assertEquals(-1000000L, summary.netCashFlow)
        assertEquals(1000000L, summary.availableBalance)
        assertEquals(0.0, summary.savingsRate, 0.01)
    }

    @Test
    fun computeSummary_handlesLeapYearsAndMonthBoundaries() {
        val leapYearTx = Transaction(
            id = "tx_leap",
            userId = "u1",
            type = "expense",
            amountMinorUnits = 500000L,
            categoryId = "cat_fuel",
            categoryNameSnapshot = "Fuel",
            transactionDate = "2024-02-29",
            transactionYearMonth = "2024-02"
        )

        val summary = FinancialEngine.computeSummary(
            selectedYearMonth = "2024-02",
            allTransactions = listOf(leapYearTx),
            categories = emptyList(),
            budget = null
        )

        assertEquals(500000L, summary.totalExpenses)
        assertTrue(summary.dailyExpenses.containsKey(29))
    }

    @Test
    fun computeSummary_preservesHistoricalCarryForwardBalance() {
        val septIncome = Transaction(
            id = "tx_sep",
            userId = "u1",
            type = "income",
            amountMinorUnits = 10000000L,
            transactionDate = "2026-09-15",
            transactionYearMonth = "2026-09"
        )
        val octExpense = Transaction(
            id = "tx_oct",
            userId = "u1",
            type = "expense",
            amountMinorUnits = 3000000L,
            transactionDate = "2026-10-05",
            transactionYearMonth = "2026-10"
        )

        val octSummary = FinancialEngine.computeSummary(
            selectedYearMonth = "2026-10",
            allTransactions = listOf(septIncome, octExpense),
            categories = emptyList(),
            budget = null,
            openingBalance = 0L
        )

        assertEquals(0L, octSummary.totalIncome)
        assertEquals(3000000L, octSummary.totalExpenses)
        assertEquals(7000000L, octSummary.availableBalance)
    }
}
