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
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class FakeClockProvider(
    private var currentInstant: Instant = Instant.parse("2026-11-15T18:00:00Z"),
    override val zoneId: ZoneId = ZoneId.of("America/Sao_Paulo")
) : ClockProvider {
    override fun nowInstant(): Instant = currentInstant
    fun advanceSeconds(seconds: Long) {
        currentInstant = currentInstant.plusSeconds(seconds)
    }
    fun setInstant(instant: Instant) {
        currentInstant = instant
    }
}

class FakeTransactionRepository(
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
                .sortedByDescending { it.occurredAtEpochMillis }
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
        shiftStartsFlow.update { map ->
            if (map.containsKey(dateKey)) map else map + (dateKey to nowMillis)
        }
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

    fun seedAt(
        date: LocalDate,
        hour: Int,
        type: TransactionType,
        amountCents: Long,
        platform: Platform? = null,
        category: ExpenseCategory? = null
    ) {
        val millis = date.atTime(hour, 0).atZone(clockProvider.zoneId).toInstant().toEpochMilli()
        val tx = Transaction(
            id = UUID.randomUUID().toString(),
            type = type,
            amountCents = amountCents,
            platform = platform,
            expenseCategory = category,
            occurredAtEpochMillis = millis,
            syncPending = true
        )
        transactionsFlow.update { it + tx }
    }
}