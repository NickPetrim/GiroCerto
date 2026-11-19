package br.com.girocerto.feature.entry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.girocerto.R
import br.com.girocerto.core.designsystem.BigActionButton
import br.com.girocerto.core.designsystem.GiroColors
import br.com.girocerto.core.util.BrlFormatter
import br.com.girocerto.domain.model.ExpenseCategory
import br.com.girocerto.domain.model.Platform
import br.com.girocerto.domain.model.TransactionType
import br.com.girocerto.domain.validator.AmountValidationError

@Composable
fun QuickEntryScreen(
    state: QuickEntryUiState,
    onDigitPressed: (Int) -> Unit,
    onBackspacePressed: () -> Unit,
    onClearPressed: () -> Unit,
    onProceedFromAmount: () -> Unit,
    onPlatformSelected: (Platform) -> Unit,
    onCategorySelected: (ExpenseCategory) -> Unit,
    onBackFromSelection: () -> Unit,
    onCorrectFromConfirmation: () -> Unit,
    onConfirmSave: () -> Unit,
    onCancelEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEarning = state.type == TransactionType.EARNING
    val headerTitle = stringResource(
        if (isEarning) R.string.entry_title_earning else R.string.entry_title_expense
    )
    val textAccent = if (isEarning) GiroColors.GainText else GiroColors.ExpenseText
    val buttonAccent = if (isEarning) GiroColors.GainGreen else GiroColors.ExpenseCoral

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = headerTitle,
            style = MaterialTheme.typography.headlineLarge,
            color = textAccent
        )

        when (state.step) {
            QuickEntryStep.AMOUNT -> AmountStepContent(
                amountCents = state.amountCents,
                validationError = state.validationError,
                textAccent = textAccent,
                buttonAccent = buttonAccent,
                onDigitPressed = onDigitPressed,
                onBackspacePressed = onBackspacePressed,
                onClearPressed = onClearPressed,
                onProceed = onProceedFromAmount,
                onCancel = onCancelEntry
            )

            QuickEntryStep.SELECTION -> SelectionStepContent(
                type = state.type,
                amountCents = state.amountCents,
                onPlatformSelected = onPlatformSelected,
                onCategorySelected = onCategorySelected,
                onBack = onBackFromSelection
            )

            QuickEntryStep.CONFIRM -> ConfirmationStepContent(
                state = state,
                textAccent = textAccent,
                onConfirm = onConfirmSave,
                onCorrect = onCorrectFromConfirmation
            )
        }
    }
}

@Composable
private fun AmountStepContent(
    amountCents: Long,
    validationError: AmountValidationError?,
    textAccent: Color,
    buttonAccent: Color,
    onDigitPressed: (Int) -> Unit,
    onBackspacePressed: () -> Unit,
    onClearPressed: () -> Unit,
    onProceed: () -> Unit,
    onCancel: () -> Unit
) {
    Text(
        text = stringResource(R.string.entry_step_value),
        style = MaterialTheme.typography.titleMedium,
        color = GiroColors.TextSecondary
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GiroColors.SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 2.dp,
            color = if (validationError != null) GiroColors.ExpenseText else textAccent
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = BrlFormatter.format(amountCents),
                style = MaterialTheme.typography.displayLarge,
                color = GiroColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            if (validationError != null) {
                Text(
                    text = stringResource(
                        when (validationError) {
                            AmountValidationError.ZERO_OR_NEGATIVE -> R.string.entry_error_zero
                            AmountValidationError.EXCEEDS_MAX -> R.string.entry_error_max
                        }
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = GiroColors.ExpenseText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    NumericKeypad(
        onDigitPressed = onDigitPressed,
        onBackspacePressed = onBackspacePressed,
        onClearPressed = onClearPressed
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BigActionButton(
            text = stringResource(R.string.entry_back),
            onClick = onCancel,
            containerColor = GiroColors.SurfaceElevated,
            contentColor = GiroColors.TextPrimary,
            borderColor = GiroColors.BorderHigh,
            modifier = Modifier.weight(1f)
        )
        BigActionButton(
            text = stringResource(R.string.entry_next),
            onClick = onProceed,
            containerColor = buttonAccent,
            contentColor = GiroColors.TextOnBrightAccent,
            modifier = Modifier.weight(1.4f)
        )
    }
}

@Composable
private fun NumericKeypad(
    onDigitPressed: (Int) -> Unit,
    onBackspacePressed: () -> Unit,
    onClearPressed: () -> Unit
) {
    val rows = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9)
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { rowDigits ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowDigits.forEach { digit ->
                    KeypadButton(
                        label = digit.toString(),
                        onClick = { onDigitPressed(digit) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val clearDesc = stringResource(R.string.entry_cd_clear)
            Button(
                onClick = onClearPressed,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = 64.dp, minHeight = 68.dp)
                    .semantics { contentDescription = clearDesc },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GiroColors.SurfaceElevated,
                    contentColor = GiroColors.AmberAlert
                ),
                border = BorderStroke(1.5.dp, GiroColors.BorderHigh)
            ) {
                Text(
                    text = stringResource(R.string.entry_clear),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            KeypadButton(
                label = "0",
                onClick = { onDigitPressed(0) },
                modifier = Modifier.weight(1f)
            )

            val backspaceDesc = stringResource(R.string.entry_cd_backspace)
            Button(
                onClick = onBackspacePressed,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minWidth = 64.dp, minHeight = 68.dp)
                    .semantics { contentDescription = backspaceDesc },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GiroColors.SurfaceElevated,
                    contentColor = GiroColors.ExpenseText
                ),
                border = BorderStroke(1.5.dp, GiroColors.BorderHigh)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_backspace),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 64.dp, minHeight = 68.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GiroColors.SurfaceCard,
            contentColor = GiroColors.TextPrimary
        ),
        border = BorderStroke(1.5.dp, GiroColors.BorderHigh)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

@Composable
private fun SelectionStepContent(
    type: TransactionType,
    amountCents: Long,
    onPlatformSelected: (Platform) -> Unit,
    onCategorySelected: (ExpenseCategory) -> Unit,
    onBack: () -> Unit
) {
    Text(
        text = BrlFormatter.format(amountCents),
        style = MaterialTheme.typography.displayMedium,
        color = GiroColors.TextPrimary
    )
    Text(
        text = stringResource(
            if (type == TransactionType.EARNING) R.string.entry_step_platform
            else R.string.entry_step_category
        ),
        style = MaterialTheme.typography.titleMedium,
        color = GiroColors.TextSecondary
    )

    if (type == TransactionType.EARNING) {
        Platform.entries.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { platform ->
                    BigActionButton(
                        text = stringResource(platformLabelRes(platform)),
                        onClick = { onPlatformSelected(platform) },
                        containerColor = GiroColors.SurfaceCard,
                        contentColor = GiroColors.TextPrimary,
                        borderColor = GiroColors.GainText,
                        minHeight = 76.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    } else {
        ExpenseCategory.entries.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { category ->
                    BigActionButton(
                        text = stringResource(categoryLabelRes(category)),
                        onClick = { onCategorySelected(category) },
                        containerColor = GiroColors.SurfaceCard,
                        contentColor = GiroColors.TextPrimary,
                        borderColor = GiroColors.ExpenseText,
                        minHeight = 76.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
    BigActionButton(
        text = stringResource(R.string.entry_back),
        onClick = onBack,
        containerColor = GiroColors.SurfaceElevated,
        contentColor = GiroColors.TextPrimary,
        borderColor = GiroColors.BorderHigh,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ConfirmationStepContent(
    state: QuickEntryUiState,
    textAccent: Color,
    onConfirm: () -> Unit,
    onCorrect: () -> Unit
) {
    Text(
        text = stringResource(R.string.entry_step_confirm),
        style = MaterialTheme.typography.titleMedium,
        color = GiroColors.AmberAlert
    )

    val selectionLabel = when (state.type) {
        TransactionType.EARNING -> state.selectedPlatform?.let { stringResource(platformLabelRes(it)) }.orEmpty()
        TransactionType.EXPENSE -> state.selectedCategory?.let { stringResource(categoryLabelRes(it)) }.orEmpty()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GiroColors.SurfaceCard,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(3.dp, textAccent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = selectionLabel,
                style = MaterialTheme.typography.headlineLarge,
                color = GiroColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            Text(
                text = BrlFormatter.format(state.amountCents),
                style = MaterialTheme.typography.displayLarge,
                color = textAccent,
                textAlign = TextAlign.Center
            )
        }
    }

    BigActionButton(
        text = stringResource(R.string.entry_confirm),
        onClick = onConfirm,
        enabled = !state.isSaving,
        containerColor = GiroColors.GainGreen,
        contentColor = GiroColors.TextOnBrightAccent,
        iconRes = R.drawable.ic_check,
        minHeight = 76.dp,
        modifier = Modifier.fillMaxWidth()
    )

    BigActionButton(
        text = stringResource(R.string.entry_correct),
        onClick = onCorrect,
        enabled = !state.isSaving,
        containerColor = GiroColors.SurfaceElevated,
        contentColor = GiroColors.TextPrimary,
        borderColor = GiroColors.AmberAlert,
        minHeight = 72.dp,
        modifier = Modifier.fillMaxWidth()
    )
}

fun platformLabelRes(platform: Platform): Int = when (platform) {
    Platform.UBER_EATS -> R.string.platform_uber_eats
    Platform.NINE_NINE -> R.string.platform_99
    Platform.IFOOD -> R.string.platform_ifood
    Platform.RAPPI -> R.string.platform_rappi
    Platform.KEETA -> R.string.platform_keeta
    Platform.ZE_DELIVERY -> R.string.platform_ze_delivery
    Platform.OUTRO -> R.string.platform_other
}

fun categoryLabelRes(category: ExpenseCategory): Int = when (category) {
    ExpenseCategory.FUEL -> R.string.category_fuel
    ExpenseCategory.MAINTENANCE -> R.string.category_maintenance
    ExpenseCategory.FOOD -> R.string.category_food
    ExpenseCategory.OTHER -> R.string.category_other
}