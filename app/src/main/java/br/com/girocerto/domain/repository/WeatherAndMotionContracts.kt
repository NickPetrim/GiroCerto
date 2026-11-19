package br.com.girocerto.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class MotionSample(
    val speedKmh: Float,
    val timestampMillis: Long,
    val latitude: Double? = null,
    val longitude: Double? = null
)

interface MotionSource {
    fun observeSpeedSamples(): Flow<MotionSample>
}

data class WeatherInfo(
    val temperatureCelsius: Int,
    val windSpeedKmh: Int,
    val windDirection: String,
    val roadCondition: String
)

interface WeatherRepository {
    fun observeCurrentWeather(): Flow<WeatherInfo>
}

class SampleWeatherRepository : WeatherRepository {
    override fun observeCurrentWeather(): Flow<WeatherInfo> = flowOf(
        WeatherInfo(
            temperatureCelsius = 24,
            windSpeedKmh = 18,
            windDirection = "SE",
            roadCondition = "Pista seca"
        )
    )
}