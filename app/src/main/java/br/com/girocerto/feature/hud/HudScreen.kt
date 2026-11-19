package br.com.girocerto.feature.hud

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.girocerto.R
import br.com.girocerto.core.designsystem.BigActionButton
import br.com.girocerto.core.designsystem.GiroColors
import br.com.girocerto.core.designsystem.HighContrastMetricCard
import br.com.girocerto.core.util.BrlFormatter
import br.com.girocerto.domain.model.ForumCategory
import br.com.girocerto.domain.model.ForumPost
import br.com.girocerto.domain.model.HomeSummary
import br.com.girocerto.feature.forum.forumCategoryLabelRes
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun HudScreen(
    hudState: HudUiState,
    homeSummary: HomeSummary,
    latestStreetAlert: ForumPost?,
    onToggleVoiceCopilot: () -> Unit,
    onExitHud: () -> Unit,
    modifier: Modifier = Modifier
) {
    val anchorStartMillis = remember(hudState.shiftStartedAtMillis) {
        hudState.shiftStartedAtMillis ?: System.currentTimeMillis()
    }
    var elapsedSeconds by remember(anchorStartMillis) {
        mutableLongStateOf(((System.currentTimeMillis() - anchorStartMillis) / 1000L).coerceAtLeast(0L))
    }

    LaunchedEffect(anchorStartMillis) {
        while (true) {
            elapsedSeconds = ((System.currentTimeMillis() - anchorStartMillis) / 1000L).coerceAtLeast(0L)
            delay(1.seconds)
        }
    }

    val remainingCents = (homeSummary.dailyGoalCents - homeSummary.netTodayCents).coerceAtLeast(0L)
    val goalStatusText = if (remainingCents == 0L) {
        stringResource(R.string.hud_goal_reached)
    } else {
        stringResource(R.string.hud_goal_remaining, BrlFormatter.format(remainingCents))
    }

    val weatherText = "${hudState.weather.temperatureCelsius} °C · " +
            "Vento ${hudState.weather.windSpeedKmh} km/h ${hudState.weather.windDirection} · " +
            hudState.weather.roadCondition

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "${stringResource(R.string.hud_title_readonly)} · ${
                stringResource(if (hudState.isAutoGpsEnabled) R.string.hud_mode_auto else R.string.hud_mode_manual)
            }",
            style = MaterialTheme.typography.titleMedium,
            color = GiroColors.AmberAlert
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HighContrastMetricCard(
                label = stringResource(R.string.hud_logged_time),
                value = formatChronometer(elapsedSeconds),
                valueColor = GiroColors.TextPrimary,
                backgroundColor = GiroColors.PureBlack,
                modifier = Modifier.weight(1.2f)
            )
            HighContrastMetricCard(
                label = stringResource(R.string.hud_speed_label),
                value = stringResource(R.string.hud_speed_value, hudState.currentSpeedKmh),
                valueColor = GiroColors.AmberAlert,
                backgroundColor = GiroColors.PureBlack,
                modifier = Modifier.weight(0.9f)
            )
        }

        HighContrastMetricCard(
            label = stringResource(R.string.hud_hourly_label),
            value = "${BrlFormatter.format(homeSummary.hourlyRateCents)}/h",
            valueColor = GiroColors.InfoCyan,
            backgroundColor = GiroColors.PureBlack,
            supportingText = goalStatusText
        )

        HighContrastMetricCard(
            label = stringResource(R.string.hud_weather_label),
            value = weatherText,
            valueColor = GiroColors.TextPrimary,
            backgroundColor = GiroColors.PureBlack
        )

        StreetAlertHudCard(latestStreetAlert = latestStreetAlert)

        BigActionButton(
            text = stringResource(
                if (hudState.isVoiceCopilotEnabled) R.string.hud_voice_on else R.string.hud_voice_off
            ),
            onClick = onToggleVoiceCopilot,
            containerColor = if (hudState.isVoiceCopilotEnabled) GiroColors.PrimaryYellow else GiroColors.SurfaceElevated,
            contentColor = if (hudState.isVoiceCopilotEnabled) GiroColors.TextOnBrightAccent else GiroColors.TextPrimary,
            borderColor = GiroColors.BorderHigh,
            minHeight = 64.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        BigActionButton(
            text = stringResource(R.string.hud_exit_manual),
            onClick = onExitHud,
            containerColor = GiroColors.SurfaceElevated,
            contentColor = GiroColors.TextPrimary,
            borderColor = GiroColors.BorderHigh,
            minHeight = 68.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StreetAlertHudCard(latestStreetAlert: ForumPost?) {
    val isUrgent = latestStreetAlert?.category == ForumCategory.BLITZ ||
            latestStreetAlert?.category == ForumCategory.ACCIDENT

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GiroColors.PureBlack,
        border = BorderStroke(
            width = 2.dp,
            color = if (isUrgent) GiroColors.PrimaryYellow else GiroColors.BorderHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.hud_street_alert_label),
                style = MaterialTheme.typography.titleMedium,
                color = if (isUrgent) GiroColors.AmberAlert else GiroColors.TextSecondary
            )
            if (latestStreetAlert == null) {
                Text(
                    text = stringResource(R.string.hud_street_alert_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = GiroColors.TextSecondary
                )
            } else {
                val categoryName = stringResource(forumCategoryLabelRes(latestStreetAlert.category))
                Text(
                    text = "$categoryName · ${latestStreetAlert.locationArea}",
                    style = MaterialTheme.typography.titleLarge,
                    color = GiroColors.TextPrimary
                )
                Text(
                    text = latestStreetAlert.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = GiroColors.TextSecondary
                )
            }
        }
    }
}

private fun formatChronometer(totalSeconds: Long): String {
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}