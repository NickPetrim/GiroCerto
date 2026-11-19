package br.com.girocerto.feature.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.girocerto.domain.model.ExpenseCategory
import br.com.girocerto.domain.model.Platform
import br.com.girocerto.domain.model.Transaction
import br.com.girocerto.domain.model.TransactionType
import br.com.girocerto.domain.repository.TransactionRepository
import br.com.girocerto.domain.validator.AmountValidationError
import br.com.girocerto.domain.validator.AmountValidationResult
import br.com.girocerto.domain.validator.TransactionAmountValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class QuickEntryStep {
    AMOUNT,
    SELECTION,
    CONFIRM
}

data class QuickEntryUiState(
    val type: TransactionType = TransactionType.EARNING,
    val step: QuickEntryStep = QuickEntryStep.AMOUNT,
    val amountCents: Long = 0L,
    val selectedPlatform: Platform? = null,
    val selectedCategory: ExpenseCategory? = null,
    val validationError: AmountValidationError? = null,
    val isSaving: Boolean = false
)

sealed interface QuickEntryEvent {
    data class Saved(val transaction: Transaction) : QuickEntryEvent
}

class QuickEntryViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TransactionRepository,
    private val validator: TransactionAmountValidator
) : ViewModel() {

    private val initialType: TransactionType = savedStateHandle.get<String>("type")
        ?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() }
        ?: TransactionType.EARNING

    private val _uiState = MutableStateFlow(QuickEntryUiState(type = initialType))
    val uiState: StateFlow<QuickEntryUiState> = _uiState.asStateFlow()

    private val _events = Channel<QuickEntryEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onDigitPressed(digit: Int) {
        if (digit !in 0..9) return
        val current = _uiState.value.amountCents
        val candidate = current * 10L + digit
        if (candidate > TransactionAmountValidator.MAX_AMOUNT_CENTS) {
            _uiState.update { it.copy(validationError = AmountValidationError.EXCEEDS_MAX) }
            return
        }
        _uiState.update {
            it.copy(
                amountCents = candidate,
                validationError = null
            )
        }
    }

    fun onBackspacePressed() {
        _uiState.update {
            it.copy(
                amountCents = it.amountCents / 10L,
                validationError = null
            )
        }
    }

    fun onClearPressed() {
        _uiState.update {
            it.copy(
                amountCents = 0L,
                validationError = null
            )
        }
    }

    fun onProceedFromAmount() {
        when (val result = validator.validate(_uiState.value.amountCents)) {
            is AmountValidationResult.Invalid -> {
                _uiState.update { it.copy(validationError = result.error) }
            }
            AmountValidationResult.Valid -> {
                _uiState.update {
                    it.copy(
                        step = QuickEntryStep.SELECTION,
                        validationError = null
                    )
                }
            }
        }
    }

    fun onPlatformSelected(platform: Platform) {
        _uiState.update {
            it.copy(
                selectedPlatform = platform,
                step = QuickEntryStep.CONFIRM
            )
        }
    }

    fun onCategorySelected(category: ExpenseCategory) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                step = QuickEntryStep.CONFIRM
            )
        }
    }

    fun onBackFromSelection() {
        _uiState.update { it.copy(step = QuickEntryStep.AMOUNT) }
    }

    fun onCorrectFromConfirmation() {
        _uiState.update { it.copy(step = QuickEntryStep.AMOUNT, validationError = null) }
    }

    fun onConfirmSave() {
        val state = _uiState.value
        if (state.isSaving) return

        when (val validation = validator.validate(state.amountCents)) {
            is AmountValidationResult.Invalid -> {
                _uiState.update {
                    it.copy(step = QuickEntryStep.AMOUNT, validationError = validation.error)
                }
                return
            }
            AmountValidationResult.Valid -> Unit
        }

        if (state.type == TransactionType.EARNING && state.selectedPlatform == null) return
        if (state.type == TransactionType.EXPENSE && state.selectedCategory == null) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val saved = repository.addTransaction(
                type = state.type,
                amountCents = state.amountCents,
                platform = state.selectedPlatform,
                expenseCategory = state.selectedCategory
            )
            _uiState.update { it.copy(isSaving = false) }
            _events.send(QuickEntryEvent.Saved(saved))
        }
    }
}