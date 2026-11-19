package br.com.girocerto.core.util

import kotlin.math.abs

object BrlFormatter {

    fun format(cents: Long): String {
        val isNegative = cents < 0L
        val absCents = abs(cents)
        val reais = absCents / 100L
        val centavos = (absCents % 100L).toString().padStart(2, '0')
        val reaisFormatted = formatThousands(reais)
        return if (isNegative) {
            "-R$ $reaisFormatted,$centavos"
        } else {
            "R$ $reaisFormatted,$centavos"
        }
    }

    private fun formatThousands(value: Long): String {
        val raw = value.toString()
        if (raw.length <= 3) return raw
        val sb = StringBuilder()
        var count = 0
        for (i in raw.length - 1 downTo 0) {
            sb.append(raw[i])
            count++
            if (count % 3 == 0 && i > 0) {
                sb.append('.')
            }
        }
        return sb.reverse().toString()
    }
}