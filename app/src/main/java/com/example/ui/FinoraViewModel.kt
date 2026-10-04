package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.calculation.FinancialEngine
import com.example.data.FinoraRepository
import com.example.model.Budget
import com.example.model.Category
import com.example.model.FinancialSummary
import com.example.model.Transaction
import com.example.model.UserProfile
import com.example.service.ExportService
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinoraViewModel(
    private val repository: FinoraRepository,
    val userId: String
) : ViewModel() {

    private val _selectedYearMonth = MutableStateFlow(YearMonth.now().toString())
    val selectedYearMonth: StateFlow<String> = _selectedYearMonth.asStateFlow()

    private val _transactionsFilter = MutableStateFlow(TransactionFilter())
    val transactionsFilter: StateFlow<TransactionFilter> = _transactionsFilter.asStateFlow()

    val transactions: StateFlow<List<Transaction>> = repository.observeTransactions(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<Category>> = repository.observeCategories(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val userProfile: StateFlow<UserProfile> = repository.observeUserProfile(userId)
        .map { it ?: UserProfile(userId = userId) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UserProfile(userId = userId)
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentBudget: StateFlow<Budget?> = _selectedYearMonth
        .flatMapLatest { ym -> repository.observeBudget(userId, ym) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    // Central Financial Calculation Engine Reactive State
    val financialSummary: StateFlow<FinancialSummary> = combine(
        _selectedYearMonth,
        transactions,
        categories,
        currentBudget,
        userProfile
    ) { ym, txList, catList, budget, profile ->
        FinancialEngine.computeSummary(
            selectedYearMonth = ym,
            allTransactions = txList,
            categories = catList,
            budget = budget,
            openingBalance = profile.openingBalanceMinorUnits
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = FinancialEngine.computeSummary(YearMonth.now().toString(), emptyList(), emptyList(), null)
    )

    // Filtered Transactions for Transactions Screen
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        transactions,
        _selectedYearMonth,
        _transactionsFilter
    ) { txList, ym, filter ->
        txList.filter { tx ->
            val matchesMonth = if (filter.allMonths) true else {
                val txYm = tx.transactionYearMonth.ifBlank {
                    try { tx.transactionDate.take(7) } catch (_: Exception) { "" }
                }
                txYm == ym
            }

            val matchesType = when (filter.type) {
                "income" -> tx.isIncome
                "expense" -> tx.isExpense
                else -> true
            }

            val matchesCategory = if (filter.categoryId != null) {
                tx.categoryId == filter.categoryId
            } else true

            val matchesSearch = if (filter.searchQuery.isNotBlank()) {
                val q = filter.searchQuery.lowercase()
                tx.categoryNameSnapshot.lowercase().contains(q) ||
                        tx.description.lowercase().contains(q) ||
                        (tx.merchant?.lowercase()?.contains(q) == true) ||
                        tx.tags.any { it.lowercase().contains(q) } ||
                        tx.paymentMethod.lowercase().contains(q)
            } else true

            matchesMonth && matchesType && matchesCategory && matchesSearch
        }.sortedWith { a, b ->
            if (filter.sortByAmount) {
                b.amountMinorUnits.compareTo(a.amountMinorUnits)
            } else {
                b.transactionDate.compareTo(a.transactionDate)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.initializeDefaultCategoriesIfEmpty(userId)
        }
    }

    fun setSelectedYearMonth(yearMonth: String) {
        _selectedYearMonth.value = yearMonth
    }

    fun updateFilter(update: (TransactionFilter) -> TransactionFilter) {
        _transactionsFilter.value = update(_transactionsFilter.value)
    }

    fun addTransaction(transaction: Transaction, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.addTransaction(transaction)
            onComplete?.invoke(result.isSuccess)
        }
    }

    fun updateTransaction(transaction: Transaction, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.updateTransaction(transaction)
            onComplete?.invoke(result.isSuccess)
        }
    }

    fun deleteTransaction(transactionId: String, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.deleteTransaction(transactionId)
            onComplete?.invoke(result.isSuccess)
        }
    }

    fun duplicateTransaction(transaction: Transaction) {
        val duplicated = transaction.copy(
            id = "",
            transactionDate = LocalDate.now().toString(),
            transactionYearMonth = YearMonth.now().toString()
        )
        addTransaction(duplicated)
    }

    fun saveBudget(budget: Budget, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.saveBudget(budget)
            onComplete?.invoke(result.isSuccess)
        }
    }

    fun copyPreviousMonthBudget() {
        val prevYm = FinancialEngine.getPreviousMonth(_selectedYearMonth.value)
        viewModelScope.launch {
            repository.observeBudget(userId, prevYm).collect { prevBudget ->
                if (prevBudget != null) {
                    val newBudget = prevBudget.copy(
                        id = "budget_${_selectedYearMonth.value.replace("-", "_")}",
                        month = _selectedYearMonth.value
                    )
                    repository.saveBudget(newBudget)
                }
            }
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    fun toggleHideBalance() {
        val current = userProfile.value
        saveUserProfile(current.copy(hideBalance = !current.hideBalance))
    }

    fun addCategory(category: Category, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.addCategory(category)
            onComplete?.invoke(result.isSuccess)
        }
    }

    fun updateCategory(category: Category, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.updateCategory(category)
            onComplete?.invoke(result.isSuccess)
        }
    }

    fun deleteCategory(categoryId: String, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.deleteCategory(categoryId)
            onComplete?.invoke(result.isSuccess)
        }
    }

    fun exportPdf(context: Context) {
        val summary = financialSummary.value
        val ym = _selectedYearMonth.value
        val monthlyTx = transactions.value.filter { it.transactionYearMonth == ym }
        val file = ExportService.generatePdf(context, summary, monthlyTx, userProfile.value.currencyCode)
        ExportService.shareFile(context, file, "application/pdf", "Share Monthly Financial Statement")
    }

    fun exportCsv(context: Context) {
        val ym = _selectedYearMonth.value
        val monthlyTx = transactions.value.filter { it.transactionYearMonth == ym }
        val file = ExportService.generateCsv(context, ym, monthlyTx)
        ExportService.shareFile(context, file, "text/csv", "Export Transactions CSV")
    }
}

data class TransactionFilter(
    val type: String = "all", // "all", "income", "expense"
    val categoryId: String? = null,
    val searchQuery: String = "",
    val sortByAmount: Boolean = false,
    val allMonths: Boolean = false
)

class FinoraViewModelFactory(
    private val repository: FinoraRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FinoraViewModel(repository, userId) as T
    }
}
