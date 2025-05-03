package com.example.budgetlyst.data

import android.content.Context
import android.content.SharedPreferences
import com.example.budgetlyst.model.Budget
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val PREFERENCES_NAME = "finance_tracker_prefs"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_BUDGET = "budget"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
    }

    // Signup or Save user data
    fun saveUser(username: String, email: String, password: String) {
        sharedPreferences.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun validateLogin(inputEmail: String, inputPassword: String): Boolean {
        val storedEmail = sharedPreferences.getString(KEY_EMAIL, null)
        val storedPassword = sharedPreferences.getString(KEY_PASSWORD, null)

        return inputEmail == storedEmail && inputPassword == storedPassword
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout() {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply()
    }

    fun getUsername(): String? = sharedPreferences.getString(KEY_USERNAME, null)
    fun getEmail(): String? = sharedPreferences.getString(KEY_EMAIL, null)

    fun setCurrency(currency: String) {
        sharedPreferences.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getCurrencySymbol(): String {
        return when (getCurrency()) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "CAD" -> "C$"
            "AUD" -> "A$"
            "INR" -> "₹"
            "CNY" -> "¥"
            "LKR" -> "Rs." // Added LKR symbol
            else -> "$" // Fallback
        }
    }

    fun getCurrency(): String {
        return sharedPreferences.getString(KEY_CURRENCY, "USD") ?: "USD"
    }

    fun setBudget(budget: Budget) {
        val budgetJson = gson.toJson(budget.toMap())
        sharedPreferences.edit().putString(KEY_BUDGET, budgetJson).apply()
    }

    fun getBudget(): Budget {
        val budgetJson = sharedPreferences.getString(KEY_BUDGET, null)
        return if (budgetJson != null) {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val budgetMap: Map<String, Any> = gson.fromJson(budgetJson, type)
            Budget.fromMap(budgetMap)
        } else {
            Budget(0.0)
        }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
    }

    fun isNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true)
    }

    fun setReminderEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
    }

    fun isReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_REMINDER_ENABLED, false)
    }
}
