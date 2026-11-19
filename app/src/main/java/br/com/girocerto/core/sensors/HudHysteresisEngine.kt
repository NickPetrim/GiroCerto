package br.com.girocerto.core.sensors

import br.com.girocerto.domain.repository.MotionSample

data class HudHysteresisConfig(
    val speedOnThresholdKmh: Float = DEFAULT_SPEED_ON_KMH,
    val onDurationMillis: Long = DEFAULT_ON_DURATION_MS,
    val speedOffThresholdKmh: Float = DEFAULT_SPEED_OFF_KMH,
    val offDurationMillis: Long = DEFAULT_OFF_DURATION_MS
) {
    init {
        require(speedOnThresholdKmh > speedOffThresholdKmh) {
            "Limiar de ativação deve ser maior que o de desativação."
        }
        require(onDurationMillis >= 0L && offDurationMillis >= 0L) {
            "Tempos de histerese não podem ser negativos."
        }
    }

    companion object {
        const val DEFAULT_SPEED_ON_KMH = 10f
        const val DEFAULT_ON_DURATION_MS = 5_000L
        const val DEFAULT_SPEED_OFF_KMH = 3f
        const val DEFAULT_OFF_DURATION_MS = 10_000L
    }
}

/**
 * Máquina de estados com histerese para evitar alternância acidental em semáforos ou ruído de GPS.
 */
class HudHysteresisEngine(
    private val config: HudHysteresisConfig = HudHysteresisConfig()
) {
    private var isHudActive: Boolean = false
    private var aboveOnSinceMillis: Long? = null
    private var belowOffSinceMillis: Long? = null

    fun onSpeedSample(sample: MotionSample): Boolean {
        val speed = sample.speedKmh.coerceAtLeast(0f)
        val now = sample.timestampMillis

        if (!isHudActive) {
            belowOffSinceMillis = null
            if (speed >= config.speedOnThresholdKmh) {
                val startedAt = aboveOnSinceMillis ?: now.also { aboveOnSinceMillis = it }
                if (now - startedAt >= config.onDurationMillis) {
                    isHudActive = true
                    aboveOnSinceMillis = null
                }
            } else {
                aboveOnSinceMillis = null
            }
        } else {
            aboveOnSinceMillis = null
            if (speed <= config.speedOffThresholdKmh) {
                val startedAt = belowOffSinceMillis ?: now.also { belowOffSinceMillis = it }
                if (now - startedAt >= config.offDurationMillis) {
                    isHudActive = false
                    belowOffSinceMillis = null
                }
            } else {
                belowOffSinceMillis = null
            }
        }

        return isHudActive
    }

    fun forceState(active: Boolean) {
        isHudActive = active
        aboveOnSinceMillis = null
        belowOffSinceMillis = null
    }
}