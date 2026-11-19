package br.com.girocerto.feature.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.girocerto.R
import br.com.girocerto.core.designsystem.BigActionButton
import br.com.girocerto.core.designsystem.GiroColors
import br.com.girocerto.core.designsystem.HighContrastMetricCard
import br.com.girocerto.core.util.BrlFormatter
import br.com.girocerto.domain.model.DailyHistoryDay
import br.com.girocerto.domain.model.Transaction
import br.com.girocerto.domain.model.TransactionType
import br.com.girocerto.feature.entry.categoryLabelRes
import br.com.girocerto.feature.entry.platformLabelRes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onTabSelected: (HistoryPeriodTab) -> Unit,
    onGoHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.history_title),
            style = MaterialTheme.typography.headlineLarge,
            color = GiroColors.TextPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BigActionButton(
                text = stringResource(R.string.history_tab_daily),
                onClick = { onTabSelected(HistoryPeriodTab.DAILY) },
                containerColor = if (state.selectedTab == HistoryPeriodTab.DAILY) {
                    GiroColors.TabSelectedBg
                } else {
                    GiroColors.SurfaceElevated
                },
                contentColor = if (state.selectedTab == HistoryPeriodTab.DAILY) {
                    GiroColors.TextOnBrightAccent
                } else {
                    GiroColors.TextPrimary
                },
                borderColor = GiroColors.BorderHigh,
                modifier = Modifier.weight(1f)
            )
            BigActionButton(
                text = stringResource(R.string.history_tab_weekly),
                onClick = { onTabSelected(HistoryPeriodTab.WEEKLY) },
                containerColor = if (state.selectedTab == HistoryPeriodTab.WEEKLY) {
                    GiroColors.TabSelectedBg
                } else {
                    GiroColors.SurfaceElevated
                },
                contentColor = if (state.selectedTab == HistoryPeriodTab.WEEKLY) {
                    GiroColors.TextOnBrightAccent
                } else {
                    GiroColors.TextPrimary
                },
                borderColor = GiroColors.BorderHigh,
                modifier = Modifier.weight(1f)
            )
        }

        when (state.selectedTab) {
            HistoryPeriodTab.DAILY -> DailyHistoryContent(
                state = state,
                onGoHomeClick = onGoHomeClick
            )
            HistoryPeriodTab.WEEKLY -> WeeklyHistoryContent(
                state = state,
                onGoHomeClick = onGoHomeClick
            )
        }
    }
}

@Composable
private fun DailyHistoryContent(
    state: HistoryUiState,
    onGoHomeClick: () -> Unit
) {
    HighContrastMetricCard(
        label = stringResource(R.string.history_daily_net),
        value = BrlFormatter.format(state.dailyNetCents),
        valueColor = netTextColor(state.dailyNetCents),
        supportingText = stringResource(
            R.string.home_today_breakdown,
            BrlFormatter.format(state.dailyGrossCents),
            BrlFormatter.format(state.dailyExpensesCents)
        )
    )

    if (state.dailyTransactions.isEmpty()) {
        EmptyHistoryCard(
            title = stringResource(R.string.history_empty_daily_title),
            description = stringResource(R.string.history_empty_daily_desc),
            onActionClick = onGoHomeClick
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.dailyTransactions.forEach { tx ->
                TransactionRowCard(transaction = tx)
            }
        }
    }
}

@Composable
private fun WeeklyHistoryContent(
    state: HistoryUiState,
    onGoHomeClick: () -> Unit
) {
    val summary = state.weeklySummary
    if (summary == null || summary.days.isEmpty()) {
        EmptyHistoryCard(
            title = stringResource(R.string.history_empty_weekly_title),
            description = stringResource(R.string.history_empty_weekly_desc),
            onActionClick = onGoHomeClick
        )
        return
    }

    val ptBr = remember { Locale.forLanguageTag("pt-BR") }
    val dateFormatter = remember(ptBr) { DateTimeFormatter.ofPattern("dd/MM", ptBr) }
    val periodLabel = "${summary.weekStart.format(dateFormatter)} – ${summary.weekEnd.format(dateFormatter)}"

    HighContrastMetricCard(
        label = "${stringResource(R.string.history_weekly_net)} ($periodLabel)",
        value = BrlFormatter.format(summary.netCents),
        valueColor = netTextColor(summary.netCents),
        supportingText = stringResource(
            R.string.home_today_breakdown,
            BrlFormatter.format(summary.grossCents),
            BrlFormatter.format(summary.expensesCents)
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        summary.days.forEach { day ->
            DaySummaryRowCard(day = day)
        }
    }
}

@Composable
private fun TransactionRowCard(transaction: Transaction) {
    val isEarning = transaction.type == TransactionType.EARNING
    val label = if (isEarning) {
        transaction.platform?.let { stringResource(platformLabelRes(it)) }.orEmpty()
    } else {
        transaction.expenseCategory?.let { stringResource(categoryLabelRes(it)) }.orEmpty()
    }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val formattedTime = remember(transaction.occurredAtEpochMillis) {
        Instant.ofEpochMilli(transaction.occurredAtEpochMillis)
            .atZone(ZoneId.of("America/Sao_Paulo"))
            .format(timeFormatter)
    }
    val signedAmount = if (isEarning) {
        "+ ${BrlFormatter.format(transaction.amountCents)}"
    } else {
        "- ${BrlFormatter.format(transaction.amountCents)}"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GiroColors.SurfaceCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, GiroColors.BorderHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge,
                    color = GiroColors.TextPrimary
                )
                Text(
                    text = "$formattedTime · ${
                        stringResource(
                            if (transaction.syncPending) R.string.history_sync_pending
                            else R.string.history_synced
                        )
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GiroColors.TextSecondary
                )
            }
            Text(
                text = signedAmount,
                style = MaterialTheme.typography.headlineLarge,
                color = if (isEarning) GiroColors.GainText else GiroColors.ExpenseText
            )
        }
    }
}

@Composable
private fun DaySummaryRowCard(day: DailyHistoryDay) {
    val ptBr = remember { Locale.forLanguageTag("pt-BR") }
    val formatter = remember(ptBr) { DateTimeFormatter.ofPattern("EEE, dd/MM", ptBr) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GiroColors.SurfaceCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, GiroColors.BorderHigh)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = day.date.format(formatter).replaceFirstChar { it.uppercase(ptBr) },
                    style = MaterialTheme.typography.titleLarge,
                    color = GiroColors.TextPrimary
                )
                Text(
                    text = BrlFormatter.format(day.netCents),
                    style = MaterialTheme.typography.headlineLarge,
                    color = netTextColor(day.netCents)
                )
            }
            Text(
                text = stringResource(
                    R.string.home_today_breakdown,
                    BrlFormatter.format(day.grossCents),
                    BrlFormatter.format(day.expensesCents)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = GiroColors.TextSecondary
            )
        }
    }
}

@Composable
private fun EmptyHistoryCard(
    title: String,
    description: String,
    onActionClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GiroColors.SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, GiroColors.BorderHigh)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = GiroColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = GiroColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            BigActionButton(
                text = stringResource(R.string.history_go_home),
                onClick = onActionClick,
                containerColor = GiroColors.PrimaryYellow,
                contentColor = GiroColors.TextOnBrightAccent,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun netTextColor(valueCents: Long) = when {
    valueCents > 0L -> GiroColors.GainText
    valueCents < 0L -> GiroColors.ExpenseText
    else -> GiroColors.TextPrimary
}