package br.com.girocerto.data.repository

import br.com.girocerto.core.util.ClockProvider
import br.com.girocerto.data.local.dao.TransactionDao
import br.com.girocerto.data.local.dao.WorkSessionDao
import br.com.girocerto.data.local.entity.TransactionEntity
import br.com.girocerto.data.local.entity.WorkSessionEntity
import br.com.girocerto.domain.model.ExpenseCategory
import br.com.girocerto.domain.model.Platform
import br.com.girocerto.domain.model.Transaction
import br.com.girocerto.domain.model.TransactionType
import br.com.girocerto.domain.repository.TransactionRepository
import br.com.girocerto.domain.validator.AmountValidationResult
import br.com.girocerto.domain.validator.TransactionAmountValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomTransactionRepository(
    private val transactionDao: TransactionDao,
    private val workSessionDao: WorkSessionDao,
    private val clockProvider: ClockProvider,
    private val validator: TransactionAmountValidator
) : TransactionRepository {

    override fun observeTransactionsBetween(
        startEpochMillis: Long,
        endEpochMillis: Long
    ): Flow<List<Transaction>> {
        return transactionDao.observeBetween(startEpochMillis, endEpochMillis)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeActiveShiftStartMillis(dateKey: String): Flow<Long?> {
        return workSessionDao.observeStartedAt(dateKey)
    }

    override suspend fun addTransaction(
        type: TransactionType,
        amountCents: Long,
        platform: Platform?,
        expenseCategory: ExpenseCategory?
    ): Transaction {
        require(validator.validate(amountCents) is AmountValidationResult.Valid) {
            "Valor monetário inválido: $amountCents"
        }
        when (type) {
            TransactionType.EARNING -> requireNotNull(platform) { "Plataforma obrigatória para ganho." }
            TransactionType.EXPENSE -> requireNotNull(expenseCategory) { "Categoria obrigatória para gasto." }
        }

        val nowMillis = clockProvider.nowEpochMillis()
        val dateKey = clockProvider.today().toString()
        workSessionDao.insertIfAbsent(WorkSessionEntity(dateKey = dateKey, startedAtEpochMillis = nowMillis))

        val entity = TransactionEntity(
            id = UUID.randomUUID().toString(),
            type = type,
            amountCents = amountCents,
            platform = if (type == TransactionType.EARNING) platform else null,
            expenseCategory = if (type == TransactionType.EXPENSE) expenseCategory else null,
            occurredAtEpochMillis = nowMillis,
            syncPending = true
        )
        transactionDao.insert(entity)
        return entity.toDomain()
    }

    override suspend fun deleteTransactionById(id: String) {
        transactionDao.deleteById(id)
    }

    private fun TransactionEntity.toDomain(): Transaction {
        return Transaction(
            id = id,
            type = type,
            amountCents = amountCents,
            platform = platform,
            expenseCategory = expenseCategory,
            occurredAtEpochMillis = occurredAtEpochMillis,
            syncPending = syncPending
        )
    }
}