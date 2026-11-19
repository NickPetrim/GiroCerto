package br.com.girocerto.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.girocerto.core.util.ClockProvider
import br.com.girocerto.data.repository.DefaultInMemoryUserProfileRepository
import br.com.girocerto.domain.model.HomeSummary
import br.com.girocerto.domain.model.Transaction
import br.com.girocerto.domain.model.TransactionType
import br.com.girocerto.domain.repository.TransactionRepository
import br.com.girocerto.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max

class HomeViewModel(
    private val repository: TransactionRepository,
    private val clockProvider: ClockProvider,
    profileRepository: UserProfileRepository
) : ViewModel() {

    constructor(
        repository: TransactionRepository,
        clockProvider: ClockProvider
    ) : this(repository, clockProvider, DefaultInMemoryUserProfileRepository())

    private val today = clockProvider.today()
    private val zoneId = clockProvider.zoneId
    private val startOfDayMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
    private val endOfDayMillis = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

    val uiState: StateFlow<HomeSummary> = combine(
        repository.observeTransactionsBetween(startOfDayMillis, endOfDayMillis),
        repository.observeActiveShiftStartMillis(today.toString()),
        profileRepository.observeProfile()
    ) { transactions, shiftStartedAt, profile ->
        calculateHomeSummary(transactions, shiftStartedAt, profile.dailyGoalCents)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeSummary()
    )

    fun undoTransaction(transactionId: String) {
        viewModelScope.launch {
            repository.deleteTransactionById(transactionId)
        }
    }

    private fun calculateHomeSummary(
        transactions: List<Transaction>,
        shiftStartedAtMillis: Long?,
        dailyGoalCents: Long
    ): HomeSummary {
        val grossCents = transactions
            .filter { it.type == TransactionType.EARNING }
            .sumOf { it.amountCents }
        val expensesCents = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amountCents }
        val netCents = grossCents - expensesCents

        val hourlyRateCents = if (transactions.isEmpty()) {
            0L
        } else {
            val nowMillis = clockProvider.nowEpochMillis()
            val started = shiftStartedAtMillis ?: nowMillis
            val elapsedHours = max(1.0, (nowMillis - started) / 3_600_000.0)
            (netCents / elapsedHours).toLong()
        }

        val goal = dailyGoalCents.coerceAtLeast(100L)
        val progress = if (netCents <= 0L) {
            0
        } else {
            ((netCents * 100L) / goal).toInt().coerceIn(0, 999)
        }

        return HomeSummary(
            netTodayCents = netCents,
            grossTodayCents = grossCents,
            expensesTodayCents = expensesCents,
            hourlyRateCents = hourlyRateCents,
            dailyGoalCents = goal,
            goalProgressPercent = progress
        )
    }
}