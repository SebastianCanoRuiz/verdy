package com.verdy.presentation.screen.plants.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verdy.domain.model.MaintenanceLog
import com.verdy.domain.model.Plant
import com.verdy.domain.model.Reminder
import com.verdy.domain.model.enums.MaintenanceAction
import com.verdy.domain.model.enums.ReminderType
import com.verdy.domain.usecase.maintenance.GetMaintenanceHistoryUseCase
import com.verdy.domain.usecase.maintenance.GetLastCareDateUseCase
import com.verdy.domain.usecase.maintenance.RegisterMaintenanceUseCase
import com.verdy.domain.usecase.plant.DeletePlantUseCase
import com.verdy.domain.usecase.plant.GetPlantByIdUseCase
import com.verdy.domain.usecase.reminder.AddReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class PlantDetailUiState(
    val plant: Plant? = null,
    val history: List<MaintenanceLog> = emptyList(),
    val lastWateringDate: LocalDate? = null,
    val lastFertilizingDate: LocalDate? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PlantDetailViewModel @Inject constructor(
    private val getPlantById: GetPlantByIdUseCase,
    private val getMaintenanceHistory: GetMaintenanceHistoryUseCase,
    private val getLastCareDate: GetLastCareDateUseCase,
    private val registerMaintenance: RegisterMaintenanceUseCase,
    private val deletePlant: DeletePlantUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlantDetailUiState())
    val uiState: StateFlow<PlantDetailUiState> = _uiState

    fun loadPlant(plantId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val plant = getPlantById(plantId)
            if (plant != null) {
                val lastWatering = getLastCareDate(plantId, ReminderType.WATERING)
                val lastFertilizing = getLastCareDate(plantId, ReminderType.FERTILIZING)
                _uiState.update {
                    it.copy(
                        plant = plant,
                        lastWateringDate = lastWatering,
                        lastFertilizingDate = lastFertilizing,
                        isLoading = false
                    )
                }
                // Observe history as Flow
                getMaintenanceHistory(plantId).collect { history ->
                    _uiState.update { it.copy(history = history) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Planta no encontrada") }
            }
        }
    }

    fun waterNow() {
        val plant = _uiState.value.plant ?: return
        viewModelScope.launch {
            registerMaintenance(plant.id, ReminderType.WATERING, MaintenanceAction.DONE)
            _uiState.update { it.copy(lastWateringDate = LocalDate.now()) }
        }
    }

    fun fertilizeNow() {
        val plant = _uiState.value.plant ?: return
        viewModelScope.launch {
            registerMaintenance(plant.id, ReminderType.FERTILIZING, MaintenanceAction.DONE)
            _uiState.update { it.copy(lastFertilizingDate = LocalDate.now()) }
        }
    }

    fun deletePlant() {
        val plant = _uiState.value.plant ?: return
        viewModelScope.launch {
            deletePlant(plant.id)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }
}
