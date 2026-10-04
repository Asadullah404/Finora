package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.FinoraRepository
import com.example.model.Budget
import com.example.model.Transaction
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FinoraRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun addAndObserveTransaction_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = FinoraRepository(firestore)

        val txId = "tx_${UUID.randomUUID().toString().replace("-", "")}"
        val tx = Transaction(
            id = txId,
            userId = uid,
            type = "income",
            amountMinorUnits = 25000000L, // 250,000 PKR
            currencyCode = "PKR",
            categoryId = "cat_salary",
            categoryNameSnapshot = "Salary",
            transactionDate = "2026-10-01",
            transactionYearMonth = "2026-10",
            description = "Monthly Salary Credit",
            paymentMethod = "Bank Transfer",
            isRecurring = true
        )

        val addResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.addTransaction(tx) }
        assertTrue("Transaction add failed: ${addResult.exceptionOrNull()?.message}", addResult.isSuccess)

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeTransactions(uid).first { list: List<Transaction> ->
                list.any { it.id == txId }
            }
        }
        assertTrue(emitted.any { it.id == txId })
        val retrieved = emitted.first { it.id == txId }
        assertEquals(25000000L, retrieved.amountMinorUnits)
        assertEquals("Salary", retrieved.categoryNameSnapshot)
    }

    @Test
    fun crossUserTransactionAccess_failsOrIsIsolated() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FinoraRepository(firestore)

        val txId = "tx_${UUID.randomUUID().toString().replace("-", "")}"
        val tx = Transaction(
            id = txId,
            userId = aliceUid,
            type = "expense",
            amountMinorUnits = 120000L,
            currencyCode = "PKR",
            categoryId = "cat_food",
            categoryNameSnapshot = "Food & Groceries",
            transactionDate = "2026-10-02",
            transactionYearMonth = "2026-10"
        )
        val addResult = withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.addTransaction(tx) }
        assertTrue(addResult.isSuccess)

        // Bob signs in and tries to query Alice's transactions collection
        signInTestUser(BOB_EMAIL)
        val bobRepo = FinoraRepository(firestore)

        var thrown: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeTransactions(aliceUid).first()
            }
        } catch (e: Throwable) {
            thrown = e
        }
        val firestoreException = (thrown as? FirebaseFirestoreException)
            ?: (thrown?.cause as? FirebaseFirestoreException)
            ?: (thrown?.cause?.cause as? FirebaseFirestoreException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }

    @Test
    fun saveAndObserveBudget_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = FinoraRepository(firestore)

        val budget = Budget(
            id = "budget_2026_10",
            userId = uid,
            month = "2026-10",
            overallLimitMinorUnits = 18000000L,
            savingsTargetMinorUnits = 7000000L,
            categoryLimits = mapOf("cat_food" to 4000000L)
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.saveBudget(budget) }
        assertTrue(saveResult.isSuccess)

        val retrievedBudget = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeBudget(uid, "2026-10").first()
        }
        assertEquals(18000000L, retrievedBudget?.overallLimitMinorUnits)
        assertEquals(7000000L, retrievedBudget?.savingsTargetMinorUnits)
    }

    @Test
    fun unauthenticatedQuery_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = FinoraRepository(firestore)

        var thrown: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeTransactions("any_user").first()
            }
        } catch (e: Throwable) {
            thrown = e
        }
        val firestoreException = (thrown as? FirebaseFirestoreException)
            ?: (thrown?.cause as? FirebaseFirestoreException)
            ?: (thrown?.cause?.cause as? FirebaseFirestoreException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_test@finora.app"
        const val BOB_EMAIL = "bob_test@finora.app"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
