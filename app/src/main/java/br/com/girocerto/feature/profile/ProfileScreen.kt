package br.com.girocerto.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.girocerto.R
import br.com.girocerto.core.designsystem.BigActionButton
import br.com.girocerto.core.designsystem.GiroColors

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onDisplayNameChange: (String) -> Unit,
    onBikeModelChange: (String) -> Unit,
    onDailyGoalChange: (String) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSaveProfile: () -> Unit,
    onOpenHudClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineLarge,
            color = GiroColors.TextPrimary
        )

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
                Text(
                    text = stringResource(R.string.profile_section_identity),
                    style = MaterialTheme.typography.titleLarge,
                    color = GiroColors.InfoCyan
                )
                OutlinedTextField(
                    value = state.displayName,
                    onValueChange = onDisplayNameChange,
                    singleLine = true,
                    label = { Text(stringResource(R.string.profile_field_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.bikeModel,
                    onValueChange = onBikeModelChange,
                    singleLine = true,
                    label = { Text(stringResource(R.string.profile_field_bike)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.dailyGoalReaisInput,
                    onValueChange = onDailyGoalChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text(stringResource(R.string.profile_field_goal)) },
                    isError = state.goalError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.goalError) {
                    Text(
                        text = stringResource(R.string.profile_error_goal),
                        style = MaterialTheme.typography.bodyMedium,
                        color = GiroColors.ExpenseText
                    )
                }
                BigActionButton(
                    text = stringResource(R.string.profile_btn_save),
                    onClick = onSaveProfile,
                    containerColor = GiroColors.PrimaryYellow,
                    contentColor = GiroColors.TextOnBrightAccent,
                    iconRes = R.drawable.ic_check,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
                Text(
                    text = stringResource(R.string.profile_section_settings),
                    style = MaterialTheme.typography.titleLarge,
                    color = GiroColors.InfoCyan
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 64.dp)
                        .clickable { onToggleDarkMode(!state.isDarkMode) }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.profile_setting_dark_mode),
                            style = MaterialTheme.typography.titleMedium,
                            color = GiroColors.TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.profile_setting_dark_mode_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = GiroColors.TextSecondary
                        )
                    }
                    Switch(
                        checked = state.isDarkMode,
                        onCheckedChange = onToggleDarkMode,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GiroColors.TextOnBrightAccent,
                            checkedTrackColor = GiroColors.PrimaryYellow
                        )
                    )
                }

                BigActionButton(
                    text = stringResource(R.string.home_btn_start_hud),
                    onClick = onOpenHudClick,
                    containerColor = GiroColors.TopBarNavy,
                    contentColor = GiroColors.TextOnNavy,
                    iconRes = R.drawable.ic_speed,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}