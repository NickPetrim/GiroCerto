package br.com.girocerto

import br.com.girocerto.core.sensors.HudHysteresisEngine
import br.com.girocerto.domain.repository.MotionSample
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HudHysteresisEngineTest {

    @Test
    fun `deve ativar apenas apos 5 segundos continuos acima ou igual a 10 kmh`() {
        val engine = HudHysteresisEngine()

        assertFalse(engine.onSpeedSample(MotionSample(15f, 0L)))
        assertFalse(engine.onSpeedSample(MotionSample(12f, 4_999L)))
        assertTrue(engine.onSpeedSample(MotionSample(10f, 5_000L)))
    }

    @Test
    fun `pico curto de GPS abaixo de 5 segundos nao deve ativar HUD`() {
        val engine = HudHysteresisEngine()

        assertFalse(engine.onSpeedSample(MotionSample(20f, 0L)))
        assertFalse(engine.onSpeedSample(MotionSample(18f, 3_000L)))
        assertFalse(engine.onSpeedSample(MotionSample(5f, 4_000L))) // Queda reinicia contagem
        assertFalse(engine.onSpeedSample(MotionSample(15f, 6_000L)))
        assertFalse(engine.onSpeedSample(MotionSample(15f, 8_000L)))
        assertTrue(engine.onSpeedSample(MotionSample(15f, 11_000L))) // 5s desde t=6000
    }

    @Test
    fun `deve manter HUD ligado em parada rapida de semaforo e desligar apos 10s a 3 kmh ou menos`() {
        val engine = HudHysteresisEngine()
        engine.forceState(true)

        // Semáforo de 7 segundos: deve permanecer ativo
        assertTrue(engine.onSpeedSample(MotionSample(0f, 10_000L)))
        assertTrue(engine.onSpeedSample(MotionSample(1f, 17_000L)))

        // Volta a andar: zera contagem de saída
        assertTrue(engine.onSpeedSample(MotionSample(12f, 18_000L)))

        // Para de vez por 10 segundos
        assertTrue(engine.onSpeedSample(MotionSample(2f, 20_000L)))
        assertTrue(engine.onSpeedSample(MotionSample(0f, 29_999L)))
        assertFalse(engine.onSpeedSample(MotionSample(3f, 30_000L)))
    }
}