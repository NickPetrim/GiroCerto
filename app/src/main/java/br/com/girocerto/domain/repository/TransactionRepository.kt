package br.com.girocerto.domain.repository

import br.com.girocerto.domain.model.ExpenseCategory
import br.com.girocerto.domain.model.Platform
import br.com.girocerto.domain.model.Transaction
import br.com.girocerto.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactionsBetween(startEpochMillis: Long, endEpochMillis: Long): Flow<List<Transaction>>
    fun observeActiveShiftStartMillis(dateKey: String): Flow<Long?>

    suspend fun addTransaction(
        type: TransactionType,
        amountCents: Long,
        platform: Platform?,
        expenseCategory: ExpenseCategory?
    ): Transaction

    suspend fun deleteTransactionById(id: String)
}