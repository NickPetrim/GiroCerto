package br.com.girocerto.feature.hud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.girocerto.core.sensors.HudHysteresisEngine
import br.com.girocerto.core.util.ClockProvider
import br.com.girocerto.core.voice.VoiceCopilot
import br.com.girocerto.domain.model.ForumCategory
import br.com.girocerto.domain.model.ForumPost
import br.com.girocerto.domain.repository.MotionSource
import br.com.girocerto.domain.repository.WeatherInfo
import br.com.girocerto.domain.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class HudUiState(
    val isHudActive: Boolean = false,
    val isAutoGpsEnabled: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val currentSpeedKmh: Int = 0,
    val shiftStartedAtMillis: Long? = null,
    val isVoiceCopilotEnabled: Boolean = true,
    val weather: WeatherInfo = WeatherInfo(24, 18, "SE", "Pista seca")
)

class HudViewModel(
    private val motionSource: MotionSource,
    private val weatherRepository: WeatherRepository,
    private val voiceCopilot: VoiceCopilot,
    private val clockProvider: ClockProvider,
    private val hysteresisEngine: HudHysteresisEngine = HudHysteresisEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HudUiState())
    val uiState: StateFlow<HudUiState> = _uiState.asStateFlow()

    private var motionCollectionJob: Job? = null
    private var manualExitCooldownUntilMillis: Long = 0L
    private var lastSpokenAlertId: String? = null

    init {
        viewModelScope.launch {
            weatherRepository.observeCurrentWeather().collect { info ->
                _uiState.update { it.copy(weather = info) }
            }
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                hasLocationPermission = granted,
                isAutoGpsEnabled = granted
            )
        }
        if (granted) {
            startObservingMotion()
        } else {
            stopObservingMotion()
        }
    }

    fun onEnterManualHud() {
        val now = clockProvider.nowEpochMillis()
        manualExitCooldownUntilMillis = 0L
        hysteresisEngine.forceState(true)
        _uiState.update {
            it.copy(
                isHudActive = true,
                shiftStartedAtMillis = it.shiftStartedAtMillis ?: now
            )
        }
    }

    fun onExitHud() {
        manualExitCooldownUntilMillis = clockProvider.nowEpochMillis() + MANUAL_EXIT_COOLDOWN_MS
        hysteresisEngine.forceState(false)
        voiceCopilot.stop()
        _uiState.update { it.copy(isHudActive = false) }
    }

    fun onToggleVoiceCopilot(spokenConfirmation: String) {
        val nextEnabled = !_uiState.value.isVoiceCopilotEnabled
        _uiState.update { it.copy(isVoiceCopilotEnabled = nextEnabled) }
        if (nextEnabled) {
            voiceCopilot.speak(spokenConfirmation)
        } else {
            voiceCopilot.stop()
        }
    }

    fun onLatestAlertChanged(alert: ForumPost?, formattedSpeech: String?) {
        val state = _uiState.value
        if (!state.isHudActive || !state.isVoiceCopilotEnabled || alert == null || formattedSpeech.isNullOrBlank()) {
            return
        }
        val isStreetUrgency = alert.category == ForumCategory.BLITZ || alert.category == ForumCategory.ACCIDENT
        if (isStreetUrgency && alert.id != lastSpokenAlertId) {
            lastSpokenAlertId = alert.id
            voiceCopilot.speak(formattedSpeech)
        }
    }

    private fun startObservingMotion() {
        motionCollectionJob?.cancel()
        motionCollectionJob = viewModelScope.launch {
            motionSource.observeSpeedSamples().collect { sample ->
                val speedRounded = sample.speedKmh.coerceAtLeast(0f).roundToInt()
                val shouldActivate = hysteresisEngine.onSpeedSample(sample)
                val now = sample.timestampMillis

                _uiState.update { current ->
                    val nextActive = if (shouldActivate && now >= manualExitCooldownUntilMillis) {
                        true
                    } else if (!shouldActivate) {
                        false
                    } else {
                        current.isHudActive
                    }
                    val nextShiftStart = if (nextActive && current.shiftStartedAtMillis == null) {
                        now
                    } else {
                        current.shiftStartedAtMillis
                    }
                    current.copy(
                        currentSpeedKmh = speedRounded,
                        isHudActive = nextActive,
                        shiftStartedAtMillis = nextShiftStart
                    )
                }
            }
        }
    }

    private fun stopObservingMotion() {
        motionCollectionJob?.cancel()
        motionCollectionJob = null
    }

    override fun onCleared() {
        stopObservingMotion()
        voiceCopilot.shutdown()
        super.onCleared()
    }

    companion object {
        private const val MANUAL_EXIT_COOLDOWN_MS = 60_000L
    }
}