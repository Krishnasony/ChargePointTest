package com.chargepoint.fleet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chargepoint.fleet.domain.model.AppError
import com.chargepoint.fleet.domain.model.Result
import com.chargepoint.fleet.domain.model.ScheduleResult
import com.chargepoint.fleet.domain.usecase.GenerateChargingScheduleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the charging schedule screen.
 * Manages UI state and coordinates with the domain layer.
 */
@HiltViewModel
class ChargingScheduleViewModel @Inject constructor(
    private val generateChargingScheduleUseCase: GenerateChargingScheduleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChargingScheduleUiState>(ChargingScheduleUiState.Initial)
    val uiState: StateFlow<ChargingScheduleUiState> = _uiState.asStateFlow()

    init {
        generateSchedule()
    }

    /**
     * Generates a new charging schedule.
     */
    fun generateSchedule() {
        viewModelScope.launch {
            _uiState.value = ChargingScheduleUiState.Loading

            when (val result = generateChargingScheduleUseCase()) {
                is Result.Success -> {
                    _uiState.value = ChargingScheduleUiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = ChargingScheduleUiState.Error(result.error)
                }
            }
        }
    }

    /**
     * Resets the UI state to initial.
     */
    fun resetState() {
        _uiState.value = ChargingScheduleUiState.Initial
    }
}

/**
 * Sealed class representing the UI state of the charging schedule screen.
 */
sealed class ChargingScheduleUiState {
    /**
     * Initial state before any action.
     */
    object Initial : ChargingScheduleUiState()

    /**
     * Loading state while generating schedule.
     */
    object Loading : ChargingScheduleUiState()

    /**
     * Success state with the generated schedule.
     */
    data class Success(val schedule: ScheduleResult) : ChargingScheduleUiState()

    /**
     * Error state with the error details.
     */
    data class Error(val error: AppError) : ChargingScheduleUiState()
}
