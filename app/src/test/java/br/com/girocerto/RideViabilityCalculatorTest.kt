package br.com.girocerto

import br.com.girocerto.domain.calculator.RideTrafficLight
import br.com.girocerto.domain.calculator.RideViabilityCalculator
import br.com.girocerto.domain.calculator.RideViabilityInput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RideViabilityCalculatorTest {

    private val calculator = RideViabilityCalculator()

    @Test
    fun `deve classificar como VERDE quando lucro por km e por hora atingem a meta`() {
        val result = calculator.evaluate(
            RideViabilityInput(
                offerCents = 1_600L, // R$ 16,00
                distanceMeters = 5_000L, // 5 km
                durationMinutes = 15,
                vehicleCostPerKmCents = 35L // R$ 1,75 de custo
            )
        )
        // Lucro: R$ 14,25 => R$ 2,85/km e R$ 57,00/h
        assertEquals(175L, result.estimatedCostCents)
        assertEquals(1_425L, result.netProfitCents)
        assertEquals(285L, result.netCentsPerKm)
        assertEquals(5_700L, result.netCentsPerHour)
        assertEquals(RideTrafficLight.GREEN, result.trafficLight)
    }

    @Test
    fun `deve classificar como AMARELO quando positivo mas abaixo da meta verde`() {
        val result = calculator.evaluate(
            RideViabilityInput(
                offerCents = 1_200L, // R$ 12,00
                distanceMeters = 8_000L, // 8 km (custo R$ 2,80 -> lucro R$ 9,20 => R$ 1,15/km)
                durationMinutes = 25
            )
        )
        assertEquals(RideTrafficLight.YELLOW, result.trafficLight)
    }

    @Test
    fun `deve classificar como VERMELHO quando lucro liquido por km for baixo ou prejuizo`() {
        val result = calculator.evaluate(
            RideViabilityInput(
                offerCents = 700L, // R$ 7,00
                distanceMeters = 9_000L, // 9 km (custo R$ 3,15 -> lucro R$ 3,85 => R$ 0,42/km)
                durationMinutes = 30
            )
        )
        assertEquals(RideTrafficLight.RED, result.trafficLight)
    }
}