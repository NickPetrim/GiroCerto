package br.com.girocerto.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val GiroDayColorScheme = lightColorScheme(
    primary = DayGiroPalette.primaryYellow,
    onPrimary = GiroColors.TextOnBrightAccent,
    secondary = DayGiroPalette.topBarNavy,
    onSecondary = GiroColors.TextOnNavy,
    error = DayGiroPalette.expenseText,
    onError = GiroColors.TextOnPanic,
    background = DayGiroPalette.background,
    onBackground = DayGiroPalette.textPrimary,
    surface = DayGiroPalette.surfaceCard,
    onSurface = DayGiroPalette.textPrimary,
    surfaceVariant = DayGiroPalette.surfaceElevated,
    onSurfaceVariant = DayGiroPalette.textSecondary,
    outline = DayGiroPalette.borderHigh
)

private val GiroNightColorScheme = darkColorScheme(
    primary = NightGiroPalette.primaryYellow,
    onPrimary = GiroColors.TextOnBrightAccent,
    secondary = NightGiroPalette.infoText,
    onSecondary = GiroColors.TextOnBrightAccent,
    error = NightGiroPalette.expenseText,
    onError = GiroColors.TextOnBrightAccent,
    background = NightGiroPalette.background,
    onBackground = NightGiroPalette.textPrimary,
    surface = NightGiroPalette.surfaceCard,
    onSurface = NightGiroPalette.textPrimary,
    surfaceVariant = NightGiroPalette.surfaceElevated,
    onSurfaceVariant = NightGiroPalette.textSecondary,
    outline = NightGiroPalette.borderHigh
)

private val GiroHudColorScheme = GiroNightColorScheme.copy(
    background = HudGiroPalette.background,
    surface = HudGiroPalette.surfaceCard
)

@Composable
fun GiroCertoTheme(
    darkTheme: Boolean = false,
    hudMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = when {
        hudMode -> HudGiroPalette
        darkTheme -> NightGiroPalette
        else -> DayGiroPalette
    }
    val materialScheme = when {
        hudMode -> GiroHudColorScheme
        darkTheme -> GiroNightColorScheme
        else -> GiroDayColorScheme
    }

    CompositionLocalProvider(LocalGiroColors provides palette) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = GiroTypography,
            content = content
        )
    }
}