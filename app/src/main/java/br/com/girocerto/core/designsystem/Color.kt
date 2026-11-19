package br.com.girocerto.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class GiroPalette(
    val background: Color,
    val surfaceCard: Color,
    val surfaceElevated: Color,
    val borderHigh: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val gainButton: Color,
    val expenseButton: Color,
    val gainText: Color,
    val expenseText: Color,
    val amberText: Color,
    val infoText: Color,
    val primaryYellow: Color,
    val topBarNavy: Color,
    val tabSelectedBg: Color
)

val DayGiroPalette = GiroPalette(
    background = Color(0xFFEEF2F6),
    surfaceCard = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFE2E8F0),
    borderHigh = Color(0xFF94A3B8),
    textPrimary = Color(0xFF0B1320),
    textSecondary = Color(0xFF334155),
    gainButton = Color(0xFF00E676),
    expenseButton = Color(0xFFFF7070),
    gainText = Color(0xFF005C2E),
    expenseText = Color(0xFFA80000),
    amberText = Color(0xFF7A4E00),
    infoText = Color(0xFF0F2744),
    primaryYellow = Color(0xFFFFC107),
    topBarNavy = Color(0xFF0F2744),
    tabSelectedBg = Color(0xFFFFC107)
)

val NightGiroPalette = GiroPalette(
    background = Color(0xFF090C10),
    surfaceCard = Color(0xFF141A23),
    surfaceElevated = Color(0xFF1E2633),
    borderHigh = Color(0xFF5A6B82),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFD0D7DE),
    gainButton = Color(0xFF00E676),
    expenseButton = Color(0xFFFF7070),
    gainText = Color(0xFF00E676),
    expenseText = Color(0xFFFF7070),
    amberText = Color(0xFFFFD600),
    infoText = Color(0xFF40C4FF),
    primaryYellow = Color(0xFFFFD600),
    topBarNavy = Color(0xFF0B1B30),
    tabSelectedBg = Color(0xFF40C4FF)
)

val HudGiroPalette = NightGiroPalette.copy(
    background = Color(0xFF000000),
    surfaceCard = Color(0xFF000000),
    surfaceElevated = Color(0xFF12161F),
    borderHigh = Color(0xFF6B7C93)
)

val LocalGiroColors = staticCompositionLocalOf { DayGiroPalette }

object GiroColors {
    val BackgroundDark: Color @Composable get() = LocalGiroColors.current.background
    val SurfaceCard: Color @Composable get() = LocalGiroColors.current.surfaceCard
    val SurfaceElevated: Color @Composable get() = LocalGiroColors.current.surfaceElevated
    val BorderHigh: Color @Composable get() = LocalGiroColors.current.borderHigh
    val TextPrimary: Color @Composable get() = LocalGiroColors.current.textPrimary
    val TextSecondary: Color @Composable get() = LocalGiroColors.current.textSecondary

    val GainGreen: Color @Composable get() = LocalGiroColors.current.gainButton
    val ExpenseCoral: Color @Composable get() = LocalGiroColors.current.expenseButton
    val PrimaryYellow: Color @Composable get() = LocalGiroColors.current.primaryYellow

    val GainText: Color @Composable get() = LocalGiroColors.current.gainText
    val ExpenseText: Color @Composable get() = LocalGiroColors.current.expenseText
    val AmberAlert: Color @Composable get() = LocalGiroColors.current.amberText
    val InfoCyan: Color @Composable get() = LocalGiroColors.current.infoText

    val TopBarNavy: Color @Composable get() = LocalGiroColors.current.topBarNavy
    val TabSelectedBg: Color @Composable get() = LocalGiroColors.current.tabSelectedBg

    val PureBlack = Color(0xFF000000)
    val PanicBackground = Color(0xFF990000)
    val PanicBorder = Color(0xFFFF5252)
    val TextOnBrightAccent = Color(0xFF000000)
    val TextOnPanic = Color(0xFFFFFFFF)
    val TextOnNavy = Color(0xFFFFFFFF)
    val TextMutedOnNavy = Color(0xFFB8C7DB)
}