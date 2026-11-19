package br.com.girocerto.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.girocerto.core.util.ClockProvider
import br.com.girocerto.domain.model.DailyHistoryDay
import br.com.girocerto.domain.model.Transaction
import br.com.girocerto.domain.model.TransactionType
import br.com.girocerto.domain.model.WeeklySummary
import br.com.girocerto.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.Instant
import java.time.temporal.TemporalAdjusters

enum class HistoryPeriodTab {
    DAILY,
    WEEKLY
}

data class HistoryUiState(
    val selectedTab: HistoryPeriodTab = HistoryPeriodTab.DAILY,
    val dailyGrossCents: Long = 0L,
    val dailyExpensesCents: Long = 0L,
    val dailyNetCents: Long = 0L,
    val dailyTransactions: List<Transaction> = emptyList(),
    val weeklySummary: WeeklySummary? = null
)

class HistoryViewModel(
    repository: TransactionRepository,
    clockProvider: ClockProvider
) : ViewModel() {

    private val zoneId = clockProvider.zoneId
    private val today = clockProvider.today()
    private val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    private val weekEndInclusive = weekStart.plusDays(6)

    private val weekStartMillis = weekStart.atStartOfDay(zoneId).toInstant().toEpochMilli()
    private val weekEndExclusiveMillis = weekStart.plusDays(7).atStartOfDay(zoneId).toInstant().toEpochMilli()

    private val selectedTab = MutableStateFlow(HistoryPeriodTab.DAILY)

    val uiState: StateFlow<HistoryUiState> = combine(
        selectedTab,
        repository.observeTransactionsBetween(weekStartMillis, weekEndExclusiveMillis)
    ) { tab, weekTransactions ->
        val groupedByDate = weekTransactions.groupBy { tx ->
            Instant.ofEpochMilli(tx.occurredAtEpochMillis).atZone(zoneId).toLocalDate()
        }

        val todayTransactions = groupedByDate[today].orEmpty()
        val dailyGross = todayTransactions
            .filter { it.type == TransactionType.EARNING }
            .sumOf { it.amountCents }
        val dailyExpenses = todayTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amountCents }

        val daysBreakdown = groupedByDate.entries
            .sortedByDescending { it.key }
            .map { (date, list) ->
                val gross = list.filter { it.type == TransactionType.EARNING }.sumOf { it.amountCents }
                val expenses = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }
                DailyHistoryDay(
                    date = date,
                    grossCents = gross,
                    expensesCents = expenses,
                    netCents = gross - expenses,
                    transactions = list
                )
            }

        val weekGross = daysBreakdown.sumOf { it.grossCents }
        val weekExpenses = daysBreakdown.sumOf { it.expensesCents }

        HistoryUiState(
            selectedTab = tab,
            dailyGrossCents = dailyGross,
            dailyExpensesCents = dailyExpenses,
            dailyNetCents = dailyGross - dailyExpenses,
            dailyTransactions = todayTransactions,
            weeklySummary = WeeklySummary(
                weekStart = weekStart,
                weekEnd = weekEndInclusive,
                grossCents = weekGross,
                expensesCents = weekExpenses,
                netCents = weekGross - weekExpenses,
                days = daysBreakdown
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HistoryUiState()
    )

    fun onTabSelected(tab: HistoryPeriodTab) {
        selectedTab.value = tab
    }
}