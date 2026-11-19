package br.com.girocerto.feature.earnings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.girocerto.R
import br.com.girocerto.core.designsystem.BigActionButton
import br.com.girocerto.core.designsystem.GiroColors
import br.com.girocerto.core.util.BrlFormatter
import br.com.girocerto.domain.model.Platform
import br.com.girocerto.domain.model.PlatformEarningsBreakdown
import br.com.girocerto.feature.entry.platformLabelRes

@Composable
fun EarningsScreen(
    state: EarningsUiState,
    onSearchQueryChange: (String) -> Unit,
    onAddRideClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onOpenHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChange,
            singleLine = true,
            placeholder = {
                Text(
                    text = stringResource(R.string.earnings_search_hint),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = GiroColors.TextSecondary
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GiroColors.SurfaceCard,
                unfocusedContainerColor = GiroColors.SurfaceCard,
                focusedBorderColor = GiroColors.TopBarNavy,
                unfocusedBorderColor = GiroColors.BorderHigh,
                focusedTextColor = GiroColors.TextPrimary,
                unfocusedTextColor = GiroColors.TextPrimary,
                focusedPlaceholderColor = GiroColors.TextSecondary,
                unfocusedPlaceholderColor = GiroColors.TextSecondary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GiroColors.SurfaceCard,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.5.dp, GiroColors.BorderHigh),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.earnings_today_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = GiroColors.TextPrimary
                )
                Text(
                    text = BrlFormatter.format(state.totalTodayCents),
                    style = MaterialTheme.typography.displayLarge,
                    color = GiroColors.TextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (state.activePlatformsCount == 0) {
                            stringResource(R.string.earnings_apps_count_zero)
                        } else {
                            stringResource(R.string.earnings_apps_count, state.activePlatformsCount)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = GiroColors.TextSecondary
                    )
                    state.activePlatforms.forEach { platform ->
                        PlatformMiniBadge(platform = platform, sizeDp = 28)
                    }
                }
            }
        }

        if (state.breakdowns.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = GiroColors.SurfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, GiroColors.BorderHigh)
            ) {
                Text(
                    text = stringResource(R.string.earnings_search_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = GiroColors.TextSecondary,
                    modifier = Modifier.padding(20.dp)
                )
            }
        } else {
            state.breakdowns.forEach { breakdown ->
                PlatformBreakdownCard(
                    breakdown = breakdown,
                    onClick = onOpenHistoryClick
                )
            }
        }

        BigActionButton(
            text = stringResource(R.string.earnings_add_ride),
            onClick = onAddRideClick,
            containerColor = GiroColors.PrimaryYellow,
            contentColor = GiroColors.TextOnBrightAccent,
            iconRes = R.drawable.ic_add,
            minHeight = 70.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BigActionButton(
                text = stringResource(R.string.earnings_add_expense),
                onClick = onAddExpenseClick,
                containerColor = GiroColors.ExpenseCoral,
                contentColor = GiroColors.TextOnBrightAccent,
                minHeight = 64.dp,
                modifier = Modifier.weight(1f)
            )
            BigActionButton(
                text = stringResource(R.string.earnings_open_history),
                onClick = onOpenHistoryClick,
                containerColor = GiroColors.SurfaceCard,
                contentColor = GiroColors.TextPrimary,
                borderColor = GiroColors.BorderHigh,
                iconRes = R.drawable.ic_history,
                minHeight = 64.dp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlatformBreakdownCard(
    breakdown: PlatformEarningsBreakdown,
    onClick: () -> Unit
) {
    val platformName = stringResource(platformLabelRes(breakdown.platform))
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = GiroColors.SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, GiroColors.BorderHigh),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PlatformMiniBadge(platform = breakdown.platform, sizeDp = 46)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.earnings_platform_prefix, platformName),
                        style = MaterialTheme.typography.titleMedium,
                        color = GiroColors.TextSecondary
                    )
                    Text(
                        text = BrlFormatter.format(breakdown.totalCents),
                        style = MaterialTheme.typography.headlineLarge,
                        color = GiroColors.TextPrimary
                    )
                }
            }
            Text(
                text = "${stringResource(R.string.earnings_entries_count, breakdown.entriesCount)} ›",
                style = MaterialTheme.typography.bodyLarge,
                color = GiroColors.TextSecondary
            )
        }
    }
}

@Composable
private fun PlatformMiniBadge(
    platform: Platform,
    sizeDp: Int
) {
    if (platform == Platform.IFOOD) {
        IfoodStyleBadge(sizeDp = sizeDp)
        return
    }

    val (bg, fg, tag) = when (platform) {
        Platform.UBER_EATS -> Triple(Color(0xFF111111), Color(0xFFFFFFFF), "Uber")
        Platform.NINE_NINE -> Triple(Color(0xFFFFD600), Color(0xFF000000), "99")
        Platform.RAPPI -> Triple(Color(0xFFFF441F), Color(0xFFFFFFFF), "Rp")
        Platform.KEETA -> Triple(Color(0xFFFFC107), Color(0xFF000000), "Kt")
        Platform.ZE_DELIVERY -> Triple(Color(0xFFFFCC00), Color(0xFF000000), "Zé")
        Platform.OUTRO -> Triple(Color(0xFF0F2744), Color(0xFFFFFFFF), "+")
        Platform.IFOOD -> Triple(Color(0xFFEA1D2C), Color(0xFFFFFFFF), "")
    }
    Surface(
        modifier = Modifier.size(sizeDp.dp),
        color = bg,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = tag,
                color = fg,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (sizeDp >= 40) 14.sp else 10.sp
            )
        }
    }
}

@Composable
private fun IfoodStyleBadge(sizeDp: Int) {
    Surface(
        modifier = Modifier.size(sizeDp.dp),
        color = Color(0xFFEA1D2C),
        shape = CircleShape
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding((sizeDp * 0.14f).dp)
        ) {
            val w = size.width
            val h = size.height

            // Olhos ovais inclinados brancos
            rotate(degrees = 14f, pivot = Offset(w * 0.34f, h * 0.37f)) {
                drawOval(
                    color = Color.White,
                    topLeft = Offset(w * 0.20f, h * 0.18f),
                    size = Size(w * 0.27f, h * 0.36f)
                )
            }
            rotate(degrees = 14f, pivot = Offset(w * 0.67f, h * 0.34f)) {
                drawOval(
                    color = Color.White,
                    topLeft = Offset(w * 0.53f, h * 0.15f),
                    size = Size(w * 0.27f, h * 0.36f)
                )
            }

            // Sorriso curvo branco
            drawArc(
                color = Color.White,
                startAngle = 22f,
                sweepAngle = 128f,
                useCenter = false,
                topLeft = Offset(w * 0.14f, h * 0.34f),
                size = Size(w * 0.70f, h * 0.48f),
                style = Stroke(width = w * 0.10f, cap = StrokeCap.Round)
            )

            // Ponta da seta à direita do sorriso
            val arrowPath = Path().apply {
                moveTo(w * 0.87f, h * 0.54f)
                lineTo(w * 0.67f, h * 0.59f)
                lineTo(w * 0.82f, h * 0.76f)
                close()
            }
            drawPath(path = arrowPath, color = Color.White)
        }
    }
}