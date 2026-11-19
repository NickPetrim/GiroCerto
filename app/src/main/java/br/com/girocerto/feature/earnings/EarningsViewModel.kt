package br.com.girocerto.feature.earnings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.girocerto.core.util.ClockProvider
import br.com.girocerto.domain.model.Platform
import br.com.girocerto.domain.model.PlatformEarningsBreakdown
import br.com.girocerto.domain.model.TransactionType
import br.com.girocerto.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class EarningsUiState(
    val searchQuery: String = "",
    val totalTodayCents: Long = 0L,
    val activePlatformsCount: Int = 0,
    val activePlatforms: List<Platform> = emptyList(),
    val breakdowns: List<PlatformEarningsBreakdown> = emptyList()
)

class EarningsViewModel(
    repository: TransactionRepository,
    clockProvider: ClockProvider
) : ViewModel() {

    private val today = clockProvider.today()
    private val zoneId = clockProvider.zoneId
    private val startOfDayMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
    private val endOfDayMillis = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<EarningsUiState> = combine(
        searchQuery,
        repository.observeTransactionsBetween(startOfDayMillis, endOfDayMillis)
    ) { query, transactions ->
        val todayEarnings = transactions.filter { it.type == TransactionType.EARNING }
        val totalCents = todayEarnings.sumOf { it.amountCents }

        val grouped = todayEarnings.groupBy { it.platform ?: Platform.OUTRO }
        val activePlatforms = Platform.entries.filter { grouped.containsKey(it) }

        val defaultOrder = listOf(Platform.UBER_EATS, Platform.NINE_NINE, Platform.IFOOD)
        val platformsToShow = if (activePlatforms.isEmpty()) {
            defaultOrder
        } else {
            (activePlatforms + defaultOrder).distinct()
        }

        val allBreakdowns = platformsToShow.map { platform ->
            val items = grouped[platform].orEmpty()
            PlatformEarningsBreakdown(
                platform = platform,
                totalCents = items.sumOf { it.amountCents },
                entriesCount = items.size
            )
        }

        val filtered = if (query.isBlank()) {
            allBreakdowns
        } else {
            val normalized = query.trim().lowercase()
            allBreakdowns.filter { item ->
                platformSearchAliases(item.platform).any { it.contains(normalized) }
            }
        }

        EarningsUiState(
            searchQuery = query,
            totalTodayCents = totalCents,
            activePlatformsCount = activePlatforms.size,
            activePlatforms = activePlatforms,
            breakdowns = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = EarningsUiState()
    )

    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
    }

    private fun platformSearchAliases(platform: Platform): List<String> = when (platform) {
        Platform.UBER_EATS -> listOf("uber", "uber eats")
        Platform.NINE_NINE -> listOf("99", "99 food", "99 moto")
        Platform.IFOOD -> listOf("ifood")
        Platform.RAPPI -> listOf("rappi")
        Platform.KEETA -> listOf("keeta")
        Platform.ZE_DELIVERY -> listOf("ze", "zé", "ze delivery", "zé delivery")
        Platform.OUTRO -> listOf("outro", "particular")
    }
}