package br.com.girocerto.domain.model

import java.time.LocalDate

enum class TransactionType {
    EARNING,
    EXPENSE
}

enum class Platform {
    UBER_EATS,
    NINE_NINE,
    IFOOD,
    RAPPI,
    KEETA,
    ZE_DELIVERY,
    OUTRO
}

enum class ExpenseCategory {
    FUEL,
    MAINTENANCE,
    FOOD,
    OTHER
}

data class Transaction(
    val id: String,
    val type: TransactionType,
    val amountCents: Long,
    val platform: Platform?,
    val expenseCategory: ExpenseCategory?,
    val occurredAtEpochMillis: Long,
    val syncPending: Boolean
)

data class HomeSummary(
    val netTodayCents: Long = 0L,
    val grossTodayCents: Long = 0L,
    val expensesTodayCents: Long = 0L,
    val hourlyRateCents: Long = 0L,
    val dailyGoalCents: Long = DEFAULT_DAILY_GOAL_CENTS,
    val goalProgressPercent: Int = 0
) {
    companion object {
        const val DEFAULT_DAILY_GOAL_CENTS = 25_000L
    }
}

data class PlatformEarningsBreakdown(
    val platform: Platform,
    val totalCents: Long,
    val entriesCount: Int
)

data class DailyHistoryDay(
    val date: LocalDate,
    val grossCents: Long,
    val expensesCents: Long,
    val netCents: Long,
    val transactions: List<Transaction>
)

data class WeeklySummary(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val grossCents: Long,
    val expensesCents: Long,
    val netCents: Long,
    val days: List<DailyHistoryDay>
)