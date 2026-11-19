package br.com.girocerto.domain.calculator

enum class RideTrafficLight {
    GREEN,
    YELLOW,
    RED
}

data class RideViabilityInput(
    val offerCents: Long,
    val distanceMeters: Long,
    val durationMinutes: Int,
    val vehicleCostPerKmCents: Long = 35L,
    val minGreenCentsPerKm: Long = 150L,
    val minYellowCentsPerKm: Long = 100L,
    val minGreenCentsPerHour: Long = 3_000L
)

data class RideViabilityResult(
    val grossCentsPerKm: Long,
    val estimatedCostCents: Long,
    val netProfitCents: Long,
    val netCentsPerKm: Long,
    val netCentsPerHour: Long,
    val trafficLight: RideTrafficLight
)

class RideViabilityCalculator {

    fun evaluate(input: RideViabilityInput): RideViabilityResult {
        require(input.offerCents >= 0L) { "Valor da oferta deve ser não negativo." }
        require(input.distanceMeters > 0L) { "Distância deve ser maior que zero." }
        require(input.durationMinutes > 0) { "Duração deve ser maior que zero." }
        require(input.vehicleCostPerKmCents >= 0L) { "Custo/km deve ser não negativo." }

        val grossCentsPerKm = (input.offerCents * 1_000L) / input.distanceMeters
        val estimatedCostCents = (input.vehicleCostPerKmCents * input.distanceMeters) / 1_000L
        val netProfitCents = input.offerCents - estimatedCostCents
        val netCentsPerKm = (netProfitCents * 1_000L) / input.distanceMeters
        val netCentsPerHour = (netProfitCents * 60L) / input.durationMinutes

        val trafficLight = when {
            netProfitCents <= 0L -> RideTrafficLight.RED
            netCentsPerKm >= input.minGreenCentsPerKm &&
                    netCentsPerHour >= input.minGreenCentsPerHour -> RideTrafficLight.GREEN
            netCentsPerKm >= input.minYellowCentsPerKm -> RideTrafficLight.YELLOW
            else -> RideTrafficLight.RED
        }

        return RideViabilityResult(
            grossCentsPerKm = grossCentsPerKm,
            estimatedCostCents = estimatedCostCents,
            netProfitCents = netProfitCents,
            netCentsPerKm = netCentsPerKm,
            netCentsPerHour = netCentsPerHour,
            trafficLight = trafficLight
        )
    }
}