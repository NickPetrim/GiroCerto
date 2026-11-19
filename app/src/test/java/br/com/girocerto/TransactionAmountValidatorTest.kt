package br.com.girocerto

import br.com.girocerto.core.util.BrlFormatter
import br.com.girocerto.domain.validator.AmountValidationError
import br.com.girocerto.domain.validator.AmountValidationResult
import br.com.girocerto.domain.validator.TransactionAmountValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TransactionAmountValidatorTest {

    private val validator = TransactionAmountValidator()

    @Test
    fun `deve bloquear valor zero e valores negativos`() {
        assertEquals(
            AmountValidationResult.Invalid(AmountValidationError.ZERO_OR_NEGATIVE),
            validator.validate(0L)
        )
        assertEquals(
            AmountValidationResult.Invalid(AmountValidationError.ZERO_OR_NEGATIVE),
            validator.validate(-500L)
        )
    }

    @Test
    fun `deve permitir valores entre 1 centavo e 9999 reais e 99 centavos`() {
        assertTrue(validator.validate(1L) is AmountValidationResult.Valid)
        assertTrue(validator.validate(4_550L) is AmountValidationResult.Valid)
        assertTrue(validator.validate(999_999L) is AmountValidationResult.Valid)
    }

    @Test
    fun `deve bloquear valores acima de 9999 reais e 99 centavos`() {
        assertEquals(
            AmountValidationResult.Invalid(AmountValidationError.EXCEEDS_MAX),
            validator.validate(1_000_000L)
        )
    }

    @Test
    fun `deve formatar centavos para padrao pt-BR sem imprecisao`() {
        assertEquals("R$ 0,00", BrlFormatter.format(0L))
        assertEquals("R$ 45,50", BrlFormatter.format(4_550L))
        assertEquals("R$ 1.250,00", BrlFormatter.format(125_000L))
        assertEquals("R$ 9.999,99", BrlFormatter.format(999_999L))
        assertEquals("-R$ 30,00", BrlFormatter.format(-3_000L))
    }
}