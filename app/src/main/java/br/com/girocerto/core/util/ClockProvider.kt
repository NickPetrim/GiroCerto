package br.com.girocerto.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

interface ClockProvider {
    val zoneId: ZoneId
    fun nowInstant(): Instant
    fun nowEpochMillis(): Long = nowInstant().toEpochMilli()
    fun today(): LocalDate = nowInstant().atZone(zoneId).toLocalDate()
}

class SystemClockProvider(
    override val zoneId: ZoneId = ZoneId.of("America/Sao_Paulo")
) : ClockProvider {
    override fun nowInstant(): Instant = Instant.now()
}