package com.example.budgetlyst.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.budgetlyst.R
import com.example.budgetlyst.data.PreferencesManager
import com.example.budgetlyst.data.TransactionRepository
import com.example.budgetlyst.databinding.ActivityMainBinding
import com.example.budgetlyst.notification.NotificationManager
import com.example.budgetlyst.ui.adapters.RecentTransactionsAdapter
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var adapter: RecentTransactionsAdapter

    private val calendar = Calendar.getInstance()

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionRepository = TransactionRepository(this)
        preferencesManager = PreferencesManager(this)
        notificationManager = NotificationManager(this)

        requestNotificationPermission()
        notificationManager.scheduleDailyReminder()

        setupBottomNavigation()
        setupMonthDisplay()
        setupSummaryCards()
        setupCategoryChart()
        setupRecentTransactions()

        binding.btnAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        binding.btnPreviousMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateDashboard()
        }

        binding.btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateDashboard()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Notification permission denied. You won't receive budget alerts.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_transactions -> {
                    startActivity(Intent(this, TransactionsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupMonthDisplay() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvCurrentMonth.text = dateFormat.format(calendar.time)
        binding.tvCurrentMonth.contentDescription = "Current month: ${binding.tvCurrentMonth.text}"
    }

    private fun setupSummaryCards() {
        val currencySymbol = preferencesManager.getCurrencySymbol()
        val totalIncome = transactionRepository.getTotalIncomeForMonth(
            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)
        )
        val totalExpenses = transactionRepository.getTotalExpensesForMonth(
            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)
        )
        val balance = totalIncome - totalExpenses

        binding.tvIncomeAmount.text = String.format("%s%.2f", currencySymbol, totalIncome)
        binding.tvExpenseAmount.text = String.format("%s%.2f", currencySymbol, totalExpenses)
        binding.tvBalanceAmount.text = String.format("%s%.2f", currencySymbol, balance)

        // Dynamic color for balance
        binding.tvBalanceAmount.setTextColor(
            if (balance >= 0) {
                binding.root.context.getColor(android.R.color.holo_green_dark)
            } else {
                binding.root.context.getColor(android.R.color.holo_red_dark)
            }
        )

        // Accessibility
        binding.tvIncomeAmount.contentDescription = "Income: $currencySymbol$totalIncome"
        binding.tvExpenseAmount.contentDescription = "Expenses: $currencySymbol$totalExpenses"
        binding.tvBalanceAmount.contentDescription = "Balance: $currencySymbol$balance"

        // Budget progress
        val budget = preferencesManager.getBudget()
        if (budget.month == calendar.get(Calendar.MONTH) &&
            budget.year == calendar.get(Calendar.YEAR) &&
            budget.amount > 0
        ) {
            val percentage = (totalExpenses / budget.amount) * 100
            binding.progressBudget.progress = percentage.toInt().coerceAtMost(100)

            val percentageText = String.format("%.1f%%", percentage)
            val budgetText = String.format(" of %s%.2f", currencySymbol, budget.amount)
            val fullText = percentageText + budgetText
            val spannable = SpannableString(fullText)

            spannable.setSpan(
                ForegroundColorSpan(Color.BLACK),
                0,
                percentageText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val statusColor = when {
                percentage >= 100 -> Color.RED
                percentage >= 80 -> Color.parseColor("#FFA500")
                else -> Color.parseColor("#006400")
            }
            spannable.setSpan(
                ForegroundColorSpan(statusColor),
                percentageText.length,
                fullText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            binding.tvBudgetStatus.text = spannable
            binding.tvBudgetStatus.setTextColor(statusColor)
            binding.tvBudgetStatus.contentDescription = "Budget progress: $fullText"
        } else {
            binding.progressBudget.progress = 0
            binding.tvBudgetStatus.text = getString(R.string.no_budget_set)
            binding.tvBudgetStatus.setTextColor(Color.GRAY)
            binding.tvBudgetStatus.contentDescription = getString(R.string.no_budget_set)
        }
    }

    private fun setupCategoryChart() {
        val expensesByCategory = transactionRepository.getExpensesByCategory(
            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)
        )

        if (expensesByCategory.isEmpty()) {
            binding.pieChart.setNoDataText(getString(R.string.no_expenses_this_month))
            binding.pieChart.setNoDataTextColor(Color.BLACK)
            binding.pieChart.contentDescription = getString(R.string.no_expenses_this_month)
            binding.pieChart.invalidate()
            return
        }

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        expensesByCategory.forEach { (category, amount) ->
            entries.add(PieEntry(amount.toFloat(), category))
            colors.add(ColorTemplate.MATERIAL_COLORS[entries.size % ColorTemplate.MATERIAL_COLORS.size])
        }

        val dataSet = PieDataSet(entries, "Categories")
        dataSet.colors = colors
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.BLACK
        dataSet.setDrawValues(true)
        dataSet.valueFormatter = PercentFormatter(binding.pieChart)
        // Move values outside the chart
        dataSet.setValueLinePart1Length(0.2f)
        dataSet.setValueLinePart2Length(0.4f)
        dataSet.setValueLineColor(Color.BLACK)
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE)

        val pieData = PieData(dataSet)
        binding.pieChart.data = pieData
        binding.pieChart.description.isEnabled = false
        binding.pieChart.centerText = getString(R.string.expenses_by_category)
        binding.pieChart.setCenterTextSize(16f)
        binding.pieChart.setCenterTextColor(Color.DKGRAY)
        binding.pieChart.legend.textSize = 12f
        binding.pieChart.legend.textColor = Color.BLACK
        binding.pieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        binding.pieChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        binding.pieChart.setEntryLabelColor(Color.BLACK)
        binding.pieChart.setEntryLabelTextSize(12f)
        binding.pieChart.animateY(1000)
        binding.pieChart.invalidate()

        // Accessibility
        val categorySummary = expensesByCategory.entries.joinToString(", ") { (category, amount) ->
            "$category: ${String.format("%s%.2f", preferencesManager.getCurrencySymbol(), amount)}"
        }
        binding.pieChart.contentDescription = "Expenses by category: $categorySummary"
    }

    private fun setupRecentTransactions() {
        val transactions = transactionRepository.getTransactionsForMonth(
            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)
        ).sortedByDescending { it.date }.take(5)

        adapter = RecentTransactionsAdapter(transactions, preferencesManager.getCurrencySymbol())
        binding.recyclerRecentTransactions.adapter = adapter

        binding.tvViewAllTransactions.setOnClickListener {
            startActivity(Intent(this, TransactionsActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }

    private fun updateDashboard() {
        setupMonthDisplay()
        setupSummaryCards()
        setupCategoryChart()
        setupRecentTransactions()
    }

    override fun onResume() {
        super.onResume()

        val budget = preferencesManager.getBudget()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        if (budget.month == currentMonth && budget.year == currentYear && budget.amount > 0) {
            val totalExpenses = transactionRepository.getTotalExpensesForMonth(currentMonth, currentYear)
            val budgetPercentage = (totalExpenses / budget.amount) * 100

            Log.d("MainActivity", "Budget: $totalExpenses / ${budget.amount} = $budgetPercentage%")
            Log.d("MainActivity", "Notifications enabled: ${preferencesManager.isNotificationEnabled()}")
        }

        updateDashboard()
        notificationManager.checkBudgetAndNotify()
    }
}