package br.com.girocerto

import app.cash.turbine.test
import br.com.girocerto.domain.model.ExpenseCategory
import br.com.girocerto.domain.model.Platform
import br.com.girocerto.domain.model.TransactionType
import br.com.girocerto.feature.history.HistoryViewModel
import br.com.girocerto.feature.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceCalculationsTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `home deve calcular lucro liquido do dia automaticamente e suportar desfazer`() = runTest {
        val clock = FakeClockProvider()
        val repository = FakeTransactionRepository(clock)
        val viewModel = HomeViewModel(repository, clock)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(0L, initial.netTodayCents)

            val earning = repository.addTransaction(
                type = TransactionType.EARNING,
                amountCents = 12_000L,
                platform = Platform.IFOOD,
                expenseCategory = null
            )
            val afterEarning = awaitItem()
            assertEquals(12_000L, afterEarning.grossTodayCents)
            assertEquals(12_000L, afterEarning.netTodayCents)

            repository.addTransaction(
                type = TransactionType.EXPENSE,
                amountCents = 3_500L,
                platform = null,
                expenseCategory = ExpenseCategory.FUEL
            )
            val afterExpense = awaitItem()
            assertEquals(12_000L, afterExpense.grossTodayCents)
            assertEquals(3_500L, afterExpense.expensesTodayCents)
            assertEquals(8_500L, afterExpense.netTodayCents)
            assertEquals(8_500L, afterExpense.hourlyRateCents)

            viewModel.undoTransaction(earning.id)
            val afterUndo = awaitItem()
            assertEquals(-3_500L, afterUndo.netTodayCents)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `historico deve calcular lucro liquido por dia, total semanal e estado vazio`() = runTest {
        val clock = FakeClockProvider()
        val repository = FakeTransactionRepository(clock)
        val viewModel = HistoryViewModel(repository, clock)

        viewModel.uiState.test {
            var emptyState = awaitItem()
            if (emptyState.weeklySummary == null) {
                emptyState = awaitItem()
            }
            assertTrue(emptyState.dailyTransactions.isEmpty())
            assertTrue(emptyState.weeklySummary?.days?.isEmpty() == true)

            repository.seedAt(
                date = LocalDate.of(2026, 11, 9),
                hour = 12,
                type = TransactionType.EARNING,
                amountCents = 18_000L,
                platform = Platform.UBER_EATS
            )
            repository.seedAt(
                date = LocalDate.of(2026, 11, 9),
                hour = 15,
                type = TransactionType.EXPENSE,
                amountCents = 4_000L,
                category = ExpenseCategory.FUEL
            )
            repository.seedAt(
                date = LocalDate.of(2026, 11, 15),
                hour = 11,
                type = TransactionType.EARNING,
                amountCents = 10_000L,
                platform = Platform.RAPPI
            )

            var latest = awaitItem()
            while ((latest.weeklySummary?.days?.size ?: 0) < 2) {
                latest = awaitItem()
            }

            assertEquals(10_000L, latest.dailyNetCents)
            assertEquals(28_000L, latest.weeklySummary?.grossCents)
            assertEquals(4_000L, latest.weeklySummary?.expensesCents)
            assertEquals(24_000L, latest.weeklySummary?.netCents)
            assertEquals(2, latest.weeklySummary?.days?.size)
            assertEquals(10_000L, latest.weeklySummary?.days?.get(0)?.netCents)
            assertEquals(14_000L, latest.weeklySummary?.days?.get(1)?.netCents)
            cancelAndIgnoreRemainingEvents()
        }
    }
}