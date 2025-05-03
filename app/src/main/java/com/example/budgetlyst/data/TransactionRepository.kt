package com.example.budgetlyst.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.budgetlyst.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Calendar

class TransactionRepository(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        TRANSACTIONS_PREFS, Context.MODE_PRIVATE
    )
    private val gson = Gson()
    private val context = context.applicationContext

    companion object {
        private const val TRANSACTIONS_PREFS = "transactions_prefs"
        private const val KEY_TRANSACTIONS = "transactions"
    }

    fun saveTransaction(transaction: Transaction) {
        val transactions = getAllTransactions().toMutableList()

        // Check if transaction exists (for updates)
        val existingIndex = transactions.indexOfFirst { it.id == transaction.id }
        if (existingIndex >= 0) {
            transactions[existingIndex] = transaction
        } else {
            transactions.add(transaction)
        }

        saveAllTransactions(transactions)
    }

    fun deleteTransaction(transactionId: String) {
        val transactions = getAllTransactions().toMutableList()
        transactions.removeIf { it.id == transactionId }
        saveAllTransactions(transactions)
    }

    fun getAllTransactions(): List<Transaction> {
        val transactionsJson = sharedPreferences.getString(KEY_TRANSACTIONS, null)
        return if (transactionsJson != null) {
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            val transactionsList: List<Map<String, Any>> = gson.fromJson(transactionsJson, type)
            transactionsList.map { Transaction.fromMap(it) }
        } else {
            emptyList()
        }
    }

    private fun saveAllTransactions(transactions: List<Transaction>) {
        val transactionsMapList = transactions.map { it.toMap() }
        val transactionsJson = gson.toJson(transactionsMapList)
        sharedPreferences.edit().putString(KEY_TRANSACTIONS, transactionsJson).apply()
    }

    fun getTransactionsForMonth(month: Int, year: Int): List<Transaction> {
        return getAllTransactions().filter { transaction ->
            val calendar = Calendar.getInstance()
            calendar.time = transaction.date
            calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year
        }
    }

    fun getExpensesForMonth(month: Int, year: Int): List<Transaction> {
        return getTransactionsForMonth(month, year).filter { it.isExpense }
    }

    fun getIncomeForMonth(month: Int, year: Int): List<Transaction> {
        return getTransactionsForMonth(month, year).filter { !it.isExpense }
    }

    fun getTotalExpensesForMonth(month: Int, year: Int): Double {
        return getExpensesForMonth(month, year).sumOf { it.amount }
    }

    fun getTotalIncomeForMonth(month: Int, year: Int): Double {
        return getIncomeForMonth(month, year).sumOf { it.amount }
    }

    fun getExpensesByCategory(month: Int, year: Int): Map<String, Double> {
        val expenses = getExpensesForMonth(month, year)
        return expenses.groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
    }

    // Backup to a specified file in internal storage
    fun backupToFile(context: Context, file: File): Boolean {
        return try {
            val transactionsJson = sharedPreferences.getString(KEY_TRANSACTIONS, null)
            if (transactionsJson.isNullOrBlank() || transactionsJson == "[]") {
                android.util.Log.w("TransactionRepository", "No transactions to back up")
                return false
            }

            FileOutputStream(file).use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(transactionsJson)
                    writer.flush()
                }
            }
            android.util.Log.d("TransactionRepository", "Backup created at: ${file.absolutePath}")
            true
        } catch (e: Exception) {
            android.util.Log.e("TransactionRepository", "Backup to file failed: ${e.message}", e)
            return false
        }
    }

    // Restore from a specified file in internal storage
    fun restoreFromFile(context: Context, file: File): Boolean {
        return try {
            if (!file.exists()) {
                android.util.Log.e("TransactionRepository", "Backup file not found: ${file.absolutePath}")
                return false
            }

            val transactionsJson = FileInputStream(file).use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                    reader.readText()
                }
            }

            // Validate JSON before committing
            if (transactionsJson.isNullOrBlank() || transactionsJson == "[]") {
                android.util.Log.e("TransactionRepository", "Backup file is empty or invalid")
                return false
            }

            // Try to parse the JSON to ensure it's valid
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            try {
                gson.fromJson<List<Map<String, Any>>>(transactionsJson, type)
            } catch (e: Exception) {
                android.util.Log.e("TransactionRepository", "Invalid JSON in backup file: ${e.message}")
                return false
            }

            // Save to SharedPreferences
            sharedPreferences.edit().putString(KEY_TRANSACTIONS, transactionsJson).apply()
            android.util.Log.d("TransactionRepository", "Restore from file successful: ${file.absolutePath}")
            true
        } catch (e: Exception) {
            android.util.Log.e("TransactionRepository", "Restore from file failed: ${e.message}", e)
            return false
        }
    }

    // Restore from a URI (used for "From File" restore option)
    fun restoreFromUri(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val transactionsJson = inputStream.bufferedReader().use { it.readText() }

                // Validate JSON before committing
                if (transactionsJson.isNullOrBlank() || transactionsJson == "[]") {
                    android.util.Log.e("TransactionRepository", "Backup file is empty or invalid")
                    return false
                }

                // Try to parse the JSON to ensure it's valid
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                try {
                    gson.fromJson<List<Map<String, Any>>>(transactionsJson, type)
                } catch (e: Exception) {
                    android.util.Log.e("TransactionRepository", "Invalid JSON in backup file: ${e.message}")
                    return false
                }

                // Save to SharedPreferences
                sharedPreferences.edit().putString(KEY_TRANSACTIONS, transactionsJson).apply()
                android.util.Log.d("TransactionRepository", "Restore from URI successful: $uri")
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("TransactionRepository", "Restore from URI failed: ${e.message}", e)
            return false
        }
    }

    // Deprecated: Remove or keep for legacy support
    fun backupToUri(context: Context, uri: Uri): Boolean {
        android.util.Log.w("TransactionRepository", "backupToUri is deprecated; use backupToFile instead")
        return false
    }

    // Deprecated: Remove or keep for legacy support
    fun restoreFromInternalStorage(context: Context): Boolean {
        android.util.Log.w("TransactionRepository", "restoreFromInternalStorage is deprecated; use restoreFromFile instead")
        return false
    }
}