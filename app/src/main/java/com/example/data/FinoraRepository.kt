package com.example.data

import android.content.Context
import com.example.R
import com.example.model.Budget
import com.example.model.Category
import com.example.model.Transaction
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.snapshots
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FinoraRepository(val firestore: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ).apply {
            // Enable offline disk caching for reliable offline behavior
            try {
                firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                    .build()
            } catch (_: Exception) {
                // Settings can only be set before Firestore is used
            }
        }
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    // --- TRANSACTIONS ---

    fun observeTransactions(userId: String): Flow<List<Transaction>> = flow {
        val path = "users/$userId/transactions"
        emitAll(
            firestore.collection("users").document(userId).collection("transactions")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Transaction::class.java).sortedByDescending { it.transactionDate }
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addTransaction(transaction: Transaction): Result<String> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val txId = if (transaction.id.isNotBlank()) transaction.id else "tx_${UUID.randomUUID().toString().replace("-", "")}"
        val txWithId = transaction.copy(id = txId, userId = uid)
        val docRef = firestore.collection("users").document(uid).collection("transactions").document(txId)

        try {
            docRef.set(txWithId.toFirestoreCreateMap()).await()
            Result.success(txId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateTransaction(transaction: Transaction): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = firestore.collection("users").document(uid).collection("transactions").document(transaction.id)

        try {
            docRef.update(transaction.toFirestoreUpdateMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(transactionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = firestore.collection("users").document(uid).collection("transactions").document(transactionId)

        try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- CATEGORIES ---

    fun observeCategories(userId: String): Flow<List<Category>> = flow {
        val path = "users/$userId/categories"
        emitAll(
            firestore.collection("users").document(userId).collection("categories")
                .snapshots()
                .map { snapshot ->
                    val list = snapshot.toObjects(Category::class.java)
                    if (list.isEmpty()) {
                        Category.defaultCategories(userId)
                    } else {
                        list.sortedBy { it.name }
                    }
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun initializeDefaultCategoriesIfEmpty(userId: String) = withContext(Dispatchers.IO) {
        val catCol = firestore.collection("users").document(userId).collection("categories")
        try {
            val snapshot = catCol.limit(1).get().await()
            if (snapshot.isEmpty) {
                val batch = firestore.batch()
                val defaults = Category.defaultCategories(userId)
                for (cat in defaults) {
                    val docRef = catCol.document(cat.id)
                    batch.set(docRef, cat.toFirestoreCreateMap())
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, catCol.path)
        }
    }

    suspend fun addCategory(category: Category): Result<String> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val catId = if (category.id.isNotBlank()) category.id else "cat_${UUID.randomUUID().toString().replace("-", "")}"
        val catWithId = category.copy(id = catId, userId = uid)
        val docRef = firestore.collection("users").document(uid).collection("categories").document(catId)

        try {
            docRef.set(catWithId.toFirestoreCreateMap()).await()
            Result.success(catId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateCategory(category: Category): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = firestore.collection("users").document(uid).collection("categories").document(category.id)

        try {
            docRef.update(category.toFirestoreUpdateMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = firestore.collection("users").document(uid).collection("categories").document(categoryId)

        try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- BUDGETS ---

    fun observeBudget(userId: String, month: String): Flow<Budget?> = flow {
        val docId = "budget_${month.replace("-", "_")}"
        val docRef = firestore.collection("users").document(userId).collection("budgets").document(docId)

        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(Budget::class.java) else null
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveBudget(budget: Budget): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docId = if (budget.id.isNotBlank()) budget.id else "budget_${budget.month.replace("-", "_")}"
        val budgetWithId = budget.copy(id = docId, userId = uid)
        val docRef = firestore.collection("users").document(uid).collection("budgets").document(docId)

        try {
            val exists = docRef.get().await().exists()
            if (exists) {
                docRef.update(budgetWithId.toFirestoreUpdateMap()).await()
            } else {
                docRef.set(budgetWithId.toFirestoreCreateMap()).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // --- USER PROFILE & SETTINGS ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val docRef = firestore.collection("users").document(userId).collection("settings").document("profile")
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = firestore.collection("users").document(uid).collection("settings").document("profile")
        val p = profile.copy(userId = uid)

        try {
            val exists = docRef.get().await().exists()
            if (exists) {
                docRef.update(p.toFirestoreUpdateMap()).await()
            } else {
                docRef.set(p.toFirestoreCreateMap()).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }
}
