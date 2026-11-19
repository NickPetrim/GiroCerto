package br.com.girocerto.feature.calculator

import androidx.lifecycle.ViewModel
import br.com.girocerto.domain.calculator.RideViabilityCalculator
import br.com.girocerto.domain.calculator.RideViabilityInput
import br.com.girocerto.domain.calculator.RideViabilityResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RideViabilityUiState(
    val offerReaisInput: String = "",
    val distanceKmInput: String = "5",
    val durationMinutesInput: String = "15",
    val costPerKmReaisInput: String = "0,35",
    val result: RideViabilityResult? = null,
    val showValidationError: Boolean = false
)

class RideViabilityViewModel(
    private val calculator: RideViabilityCalculator = RideViabilityCalculator()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideViabilityUiState())
    val uiState: StateFlow<RideViabilityUiState> = _uiState.asStateFlow()

    fun onOfferChange(value: String) {
        _uiState.update { it.copy(offerReaisInput = sanitizeDecimalInput(value), showValidationError = false) }
    }

    fun onDistanceChange(value: String) {
        _uiState.update { it.copy(distanceKmInput = sanitizeDecimalInput(value), showValidationError = false) }
    }

    fun onDurationChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(3)
        _uiState.update { it.copy(durationMinutesInput = digits, showValidationError = false) }
    }

    fun onCostPerKmChange(value: String) {
        _uiState.update { it.copy(costPerKmReaisInput = sanitizeDecimalInput(value), showValidationError = false) }
    }

    fun onEvaluate() {
        val state = _uiState.value
        val offerCents = parseDecimalToCents(state.offerReaisInput)
        val distanceMeters = parseDecimalToMeters(state.distanceKmInput)
        val durationMin = state.durationMinutesInput.toIntOrNull() ?: 0
        val costPerKmCents = parseDecimalToCents(state.costPerKmReaisInput)

        if (offerCents <= 0L || distanceMeters <= 0L || durationMin <= 0) {
            _uiState.update { it.copy(showValidationError = true, result = null) }
            return
        }

        val evaluated = calculator.evaluate(
            RideViabilityInput(
                offerCents = offerCents,
                distanceMeters = distanceMeters,
                durationMinutes = durationMin,
                vehicleCostPerKmCents = costPerKmCents
            )
        )
        _uiState.update { it.copy(result = evaluated, showValidationError = false) }
    }

    fun onClear() {
        _uiState.value = RideViabilityUiState()
    }

    private fun sanitizeDecimalInput(raw: String): String {
        val cleaned = raw.filter { it.isDigit() || it == ',' || it == '.' }
        return cleaned.take(8)
    }

    private fun parseDecimalToCents(raw: String): Long {
        val normalized = raw.trim().replace(',', '.')
        if (normalized.isEmpty()) return 0L
        val parts = normalized.split('.')
        val integerPart = parts.getOrNull(0)?.filter { it.isDigit() }?.toLongOrNull() ?: 0L
        val fracRaw = parts.getOrNull(1)?.filter { it.isDigit() }.orEmpty()
        val fracCents = when (fracRaw.length) {
            0 -> 0L
            1 -> (fracRaw[0] - '0') * 10L
            else -> fracRaw.substring(0, 2).toLongOrNull() ?: 0L
        }
        return integerPart * 100L + fracCents
    }

    private fun parseDecimalToMeters(raw: String): Long {
        val normalized = raw.trim().replace(',', '.')
        if (normalized.isEmpty()) return 0L
        val parts = normalized.split('.')
        val kmPart = parts.getOrNull(0)?.filter { it.isDigit() }?.toLongOrNull() ?: 0L
        val fracRaw = parts.getOrNull(1)?.filter { it.isDigit() }.orEmpty().padEnd(3, '0')
        val metersFrac = fracRaw.substring(0, 3).toLongOrNull() ?: 0L
        return kmPart * 1_000L + metersFrac
    }
}