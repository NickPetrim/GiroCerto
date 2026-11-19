package br.com.girocerto.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.girocerto.domain.model.EmergencyChannel
import br.com.girocerto.domain.repository.UserProfileRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val displayName: String = "",
    val bikeModel: String = "",
    val dailyGoalReaisInput: String = "250",
    val isDarkMode: Boolean = false,
    val goalError: Boolean = false
)

sealed interface ProfileEvent {
    data object Saved : ProfileEvent
}

class ProfileViewModel(
    private val profileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var loadedInitial = false

    init {
        viewModelScope.launch {
            profileRepository.observeProfile().collect { profile ->
                if (!loadedInitial) {
                    loadedInitial = true
                    _uiState.value = ProfileUiState(
                        displayName = profile.displayName,
                        bikeModel = profile.bikeModel,
                        dailyGoalReaisInput = (profile.dailyGoalCents / 100L).toString(),
                        isDarkMode = profile.isDarkMode
                    )
                } else {
                    _uiState.update { it.copy(isDarkMode = profile.isDarkMode) }
                }
            }
        }
    }

    fun onDisplayNameChange(value: String) {
        _uiState.update { it.copy(displayName = value) }
    }

    fun onBikeModelChange(value: String) {
        _uiState.update { it.copy(bikeModel = value) }
    }

    fun onDailyGoalChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(dailyGoalReaisInput = digitsOnly, goalError = false) }
    }

    fun onToggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            profileRepository.setDarkMode(enabled)
        }
    }

    fun onSaveProfile() {
        val state = _uiState.value
        val goalReais = state.dailyGoalReaisInput.toLongOrNull() ?: 0L
        if (goalReais < 10L || goalReais > 9_999L) {
            _uiState.update { it.copy(goalError = true) }
            return
        }
        viewModelScope.launch {
            profileRepository.updateProfile(
                displayName = state.displayName,
                bikeModel = state.bikeModel,
                dailyGoalCents = goalReais * 100L,
                emergencyContactName = "",
                emergencyContactPhone = "",
                emergencyChannel = EmergencyChannel.WHATSAPP
            )
            _events.send(ProfileEvent.Saved)
        }
    }
}