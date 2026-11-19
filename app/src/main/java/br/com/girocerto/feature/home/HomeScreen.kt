package br.com.girocerto.feature.home

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.girocerto.R
import br.com.girocerto.core.designsystem.BigActionButton
import br.com.girocerto.core.designsystem.GiroColors
import br.com.girocerto.core.designsystem.HighContrastMetricCard
import br.com.girocerto.core.util.BrlFormatter
import br.com.girocerto.domain.model.HomeSummary

@Composable
fun HomeScreen(
    summary: HomeSummary,
    onAddEarningClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    isAutoGpsEnabled: Boolean = false,
    onEnableAutoGpsClick: () -> Unit = {},
    onOpenCalculatorClick: () -> Unit = {},
    onEnterHudClick: () -> Unit = {},
    onOpenHistoryClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val netColor = when {
        summary.netTodayCents > 0L -> GiroColors.GainText
        summary.netTodayCents < 0L -> GiroColors.ExpenseText
        else -> GiroColors.TextPrimary
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HighContrastMetricCard(
            label = stringResource(R.string.home_net_today_title),
            value = BrlFormatter.format(summary.netTodayCents),
            valueColor = netColor,
            supportingText = stringResource(
                R.string.home_today_breakdown,
                BrlFormatter.format(summary.grossTodayCents),
                BrlFormatter.format(summary.expensesTodayCents)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HighContrastMetricCard(
                label = stringResource(R.string.home_hourly_rate_label),
                value = "${BrlFormatter.format(summary.hourlyRateCents)}/h",
                valueColor = GiroColors.InfoCyan,
                modifier = Modifier.weight(1f)
            )
            HighContrastMetricCard(
                label = stringResource(R.string.home_daily_goal_label),
                value = BrlFormatter.format(summary.dailyGoalCents),
                valueColor = GiroColors.AmberAlert,
                supportingText = stringResource(R.string.home_goal_progress, summary.goalProgressPercent),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BigActionButton(
                text = stringResource(R.string.home_btn_add_earning),
                onClick = onAddEarningClick,
                containerColor = GiroColors.GainGreen,
                contentColor = GiroColors.TextOnBrightAccent,
                iconRes = R.drawable.ic_add,
                accessibilityLabel = stringResource(R.string.home_cd_add_earning),
                minHeight = 76.dp,
                modifier = Modifier.weight(1f)
            )
            BigActionButton(
                text = stringResource(R.string.home_btn_add_expense),
                onClick = onAddExpenseClick,
                containerColor = GiroColors.ExpenseCoral,
                contentColor = GiroColors.TextOnBrightAccent,
                iconRes = R.drawable.ic_remove,
                accessibilityLabel = stringResource(R.string.home_cd_add_expense),
                minHeight = 76.dp,
                modifier = Modifier.weight(1f)
            )
        }

        BigActionButton(
            text = stringResource(R.string.home_btn_calculator),
            onClick = onOpenCalculatorClick,
            containerColor = GiroColors.PrimaryYellow,
            contentColor = GiroColors.TextOnBrightAccent,
            iconRes = R.drawable.ic_speed,
            minHeight = 68.dp,
            modifier = Modifier.fillMaxWidth()
        )

        BigActionButton(
            text = stringResource(R.string.home_btn_start_hud),
            onClick = onEnterHudClick,
            containerColor = GiroColors.TopBarNavy,
            contentColor = GiroColors.TextOnNavy,
            borderColor = GiroColors.PrimaryYellow,
            iconRes = R.drawable.ic_speed,
            accessibilityLabel = stringResource(R.string.home_cd_start_hud),
            minHeight = 68.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GiroColors.SurfaceCard,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, GiroColors.BorderHigh)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(
                        if (isAutoGpsEnabled) R.string.gps_status_auto_on
                        else R.string.gps_status_manual_only
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isAutoGpsEnabled) GiroColors.GainText else GiroColors.TextSecondary
                )
                if (!isAutoGpsEnabled) {
                    BigActionButton(
                        text = stringResource(R.string.gps_btn_enable_auto),
                        onClick = onEnableAutoGpsClick,
                        containerColor = GiroColors.SurfaceElevated,
                        contentColor = GiroColors.TextPrimary,
                        borderColor = GiroColors.BorderHigh,
                        minHeight = 64.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        BigActionButton(
            text = stringResource(R.string.home_btn_history),
            onClick = onOpenHistoryClick,
            containerColor = GiroColors.SurfaceCard,
            contentColor = GiroColors.TextPrimary,
            borderColor = GiroColors.BorderHigh,
            iconRes = R.drawable.ic_history,
            minHeight = 64.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}