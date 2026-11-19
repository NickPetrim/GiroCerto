package br.com.girocerto.domain.validator

enum class AmountValidationError {
    ZERO_OR_NEGATIVE,
    EXCEEDS_MAX
}

sealed interface AmountValidationResult {
    data object Valid : AmountValidationResult
    data class Invalid(val error: AmountValidationError) : AmountValidationResult
}

class TransactionAmountValidator {

    fun validate(amountCents: Long): AmountValidationResult {
        return when {
            amountCents <= 0L -> AmountValidationResult.Invalid(AmountValidationError.ZERO_OR_NEGATIVE)
            amountCents > MAX_AMOUNT_CENTS -> AmountValidationResult.Invalid(AmountValidationError.EXCEEDS_MAX)
            else -> AmountValidationResult.Valid
        }
    }

    companion object {
        const val MAX_AMOUNT_CENTS: Long = 999_999L
    }
}