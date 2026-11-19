package br.com.girocerto

import br.com.girocerto.core.util.ClockProvider
import br.com.girocerto.domain.model.ExpenseCategory
import br.com.girocerto.domain.model.Platform
import br.com.girocerto.domain.model.Transaction
import br.com.girocerto.domain.model.TransactionType
import br.com.girocerto.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

class FakeUiClockProvider : ClockProvider {
    override val zoneId: ZoneId = ZoneId.of("America/Sao_Paulo")
    override fun nowInstant(): Instant = Instant.parse("2026-11-15T18:00:00Z")
}

class FakeUiTransactionRepository(
    private val clockProvider: ClockProvider
) : TransactionRepository {

    private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())
    private val shiftStartsFlow = MutableStateFlow<Map<String, Long>>(emptyMap())

    override fun observeTransactionsBetween(
        startEpochMillis: Long,
        endEpochMillis: Long
    ): Flow<List<Transaction>> {
        return transactionsFlow.map { list ->
            list.filter { it.occurredAtEpochMillis in startEpochMillis until endEpochMillis }
        }
    }

    override fun observeActiveShiftStartMillis(dateKey: String): Flow<Long?> {
        return shiftStartsFlow.map { it[dateKey] }
    }

    override suspend fun addTransaction(
        type: TransactionType,
        amountCents: Long,
        platform: Platform?,
        expenseCategory: ExpenseCategory?
    ): Transaction {
        val nowMillis = clockProvider.nowEpochMillis()
        val dateKey = clockProvider.today().toString()
        shiftStartsFlow.update { it + (dateKey to (it[dateKey] ?: nowMillis)) }

        val tx = Transaction(
            id = UUID.randomUUID().toString(),
            type = type,
            amountCents = amountCents,
            platform = platform,
            expenseCategory = expenseCategory,
            occurredAtEpochMillis = nowMillis,
            syncPending = true
        )
        transactionsFlow.update { it + tx }
        return tx
    }

    override suspend fun deleteTransactionById(id: String) {
        transactionsFlow.update { list -> list.filterNot { it.id == id } }
    }
}