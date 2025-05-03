package com.example.budgetlyst.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetlyst.databinding.ItemTransactionBinding
import com.example.budgetlyst.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionsAdapter(
    private var transactions: List<Transaction>,
    private val currencySymbol: String,
    private val listener: OnTransactionClickListener
) : RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    interface OnTransactionClickListener {
        fun onTransactionClick(transaction: Transaction)
        fun onTransactionLongClick(transaction: Transaction)
    }

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        init {
            // Set click listener for the edit button
            binding.btnEdit.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onTransactionClick(transactions[position])
                }
            }

            // Set click listener for the delete button
            binding.btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onTransactionLongClick(transactions[position])
                }
            }
        }

        fun bind(transaction: Transaction) {
            binding.tvTitle.text = transaction.title
            binding.tvCategory.text = transaction.category
            binding.tvDate.text = dateFormatter.format(transaction.date)

            // Format amount with currency symbol and two decimal places
            val amountText = if (transaction.isExpense) {
                "-%s%.2f".format(currencySymbol, transaction.amount)
            } else {
                "+%s%.2f".format(currencySymbol, transaction.amount)
            }

            binding.tvAmount.text = amountText
            binding.tvAmount.setTextColor(
                if (transaction.isExpense) {
                    binding.root.context.getColor(android.R.color.holo_red_dark)
                } else {
                    binding.root.context.getColor(android.R.color.holo_green_dark)
                }
            )
        }
    }
}