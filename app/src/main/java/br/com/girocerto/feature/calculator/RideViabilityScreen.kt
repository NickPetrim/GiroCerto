package br.com.girocerto.feature.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.girocerto.R
import br.com.girocerto.core.designsystem.BigActionButton
import br.com.girocerto.core.designsystem.GiroColors
import br.com.girocerto.core.util.BrlFormatter
import br.com.girocerto.domain.calculator.RideTrafficLight
import br.com.girocerto.domain.calculator.RideViabilityResult

@Composable
fun RideViabilityScreen(
    state: RideViabilityUiState,
    onOfferChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onCostPerKmChange: (String) -> Unit,
    onEvaluate: () -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.calc_title),
            style = MaterialTheme.typography.headlineLarge,
            color = GiroColors.TextPrimary
        )
        Text(
            text = stringResource(R.string.calc_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = GiroColors.TextSecondary
        )

        state.result?.let { result ->
            TrafficLightResultCard(result = result)
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GiroColors.SurfaceCard,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, GiroColors.BorderHigh)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.offerReaisInput,
                    onValueChange = onOfferChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = { Text(stringResource(R.string.calc_field_offer)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = state.distanceKmInput,
                        onValueChange = onDistanceChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        label = { Text(stringResource(R.string.calc_field_distance)) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.durationMinutesInput,
                        onValueChange = onDurationChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(stringResource(R.string.calc_field_duration)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = stringResource(R.string.calc_quick_km),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GiroColors.TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("2", "4", "6", "8", "10").forEach { km ->
                        QuickPresetChip(
                            label = "${km}km",
                            selected = state.distanceKmInput == km,
                            onClick = { onDistanceChange(km) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.calc_quick_min),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GiroColors.TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("10", "15", "20", "30").forEach { min ->
                        QuickPresetChip(
                            label = "${min}m",
                            selected = state.durationMinutesInput == min,
                            onClick = { onDurationChange(min) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = state.costPerKmReaisInput,
                    onValueChange = onCostPerKmChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = { Text(stringResource(R.string.calc_field_cost_km)) },
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.showValidationError) {
                    Text(
                        text = stringResource(R.string.calc_error_invalid),
                        style = MaterialTheme.typography.bodyLarge,
                        color = GiroColors.ExpenseText
                    )
                }

                BigActionButton(
                    text = stringResource(R.string.calc_btn_evaluate),
                    onClick = onEvaluate,
                    containerColor = GiroColors.PrimaryYellow,
                    contentColor = GiroColors.TextOnBrightAccent,
                    iconRes = R.drawable.ic_check,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BigActionButton(
                text = stringResource(R.string.calc_btn_clear),
                onClick = onClear,
                containerColor = GiroColors.SurfaceElevated,
                contentColor = GiroColors.TextPrimary,
                borderColor = GiroColors.BorderHigh,
                modifier = Modifier.weight(1f)
            )
            BigActionButton(
                text = stringResource(R.string.entry_back),
                onClick = onBack,
                containerColor = GiroColors.TopBarNavy,
                contentColor = GiroColors.TextOnNavy,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickPresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 64.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) GiroColors.TabSelectedBg else GiroColors.SurfaceElevated,
            contentColor = if (selected) GiroColors.TextOnBrightAccent else GiroColors.TextPrimary
        ),
        border = BorderStroke(1.5.dp, GiroColors.BorderHigh)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TrafficLightResultCard(result: RideViabilityResult) {
    val (bg, fg, titleRes) = when (result.trafficLight) {
        RideTrafficLight.GREEN -> Triple(Color(0xFF00E676), Color(0xFF000000), R.string.calc_verdict_green)
        RideTrafficLight.YELLOW -> Triple(Color(0xFFFFC107), Color(0xFF000000), R.string.calc_verdict_yellow)
        RideTrafficLight.RED -> Triple(Color(0xFF990000), Color(0xFFFFFFFF), R.string.calc_verdict_red)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bg,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(2.dp, Color(0xFF000000))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge,
                color = fg,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.calc_metric_net_profit, BrlFormatter.format(result.netProfitCents)),
                style = MaterialTheme.typography.headlineLarge,
                color = fg
            )
            Text(
                text = stringResource(
                    R.string.calc_metric_net_km,
                    BrlFormatter.format(result.netCentsPerKm),
                    BrlFormatter.format(result.grossCentsPerKm)
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = fg,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(
                    R.string.calc_metric_net_hour,
                    BrlFormatter.format(result.netCentsPerHour),
                    BrlFormatter.format(result.estimatedCostCents)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = fg
            )
        }
    }
}