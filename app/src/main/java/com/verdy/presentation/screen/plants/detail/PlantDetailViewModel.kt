package com.verdy.presentation.screen.plants.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verdy.domain.model.MaintenanceLog
import com.verdy.domain.model.Plant
import com.verdy.domain.model.PlantHealthEvaluationResult
import com.verdy.domain.model.enums.IdentificationConfidence
import com.verdy.domain.model.enums.MaintenanceAction
import com.verdy.domain.model.enums.ReminderType
import com.verdy.domain.usecase.ai.EvaluatePlantHealthUseCase
import com.verdy.domain.usecase.ai.GetPlantCuriositiesUseCase
import com.verdy.domain.usecase.maintenance.GetMaintenanceHistoryUseCase
import com.verdy.domain.usecase.maintenance.GetLastCareDateUseCase
import com.verdy.domain.usecase.maintenance.RegisterMaintenanceUseCase
import com.verdy.domain.usecase.plant.DeletePlantUseCase
import com.verdy.domain.usecase.plant.GetPlantByIdUseCase
import com.verdy.domain.usecase.plant.UpdatePlantUseCase
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
    val curiosities: String? = null,
    val loadingCuriosities: Boolean = false,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val isEvaluatingHealth: Boolean = false,
    val healthEvaluation: PlantHealthEvaluationResult? = null,
    val showHealthEvaluationSheet: Boolean = false,
    val pendingHealthEvaluation: PlantHealthEvaluationResult? = null,
    val error: String? = null
)

@HiltViewModel
class PlantDetailViewModel @Inject constructor(
    private val getPlantById: GetPlantByIdUseCase,
    private val getMaintenanceHistory: GetMaintenanceHistoryUseCase,
    private val getLastCareDate: GetLastCareDateUseCase,
    private val registerMaintenance: RegisterMaintenanceUseCase,
    private val deletePlant: DeletePlantUseCase,
    private val updatePlant: UpdatePlantUseCase,
    private val getPlantCuriosities: GetPlantCuriositiesUseCase,
    private val evaluatePlantHealth: EvaluatePlantHealthUseCase
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
                        curiosities = plant.aiCuriosities,
                        lastWateringDate = lastWatering,
                        lastFertilizingDate = lastFertilizing,
                        isLoading = false
                    )
                }
                if (plant.aiCuriosities == null) {
                    fetchAndCacheCuriosities(plant)
                }
                getMaintenanceHistory(plantId).collect { history ->
                    _uiState.update { it.copy(history = history) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Planta no encontrada") }
            }
        }
    }

    fun openHealthEvaluation() {
        _uiState.update { it.copy(showHealthEvaluationSheet = true, healthEvaluation = null, error = null) }
    }

    fun dismissHealthEvaluation() {
        _uiState.update {
            it.copy(
                showHealthEvaluationSheet = false,
                healthEvaluation = null,
                pendingHealthEvaluation = null,
                isEvaluatingHealth = false
            )
        }
    }

    fun evaluateHealth(imagePath: String) {
        val plant = _uiState.value.plant ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isEvaluatingHealth = true, error = null) }
            val name = plant.commonName
            val scientific = plant.scientificName
            evaluatePlantHealth(imagePath, name, scientific, plant.status).fold(
                onSuccess = { result ->
                    if (result.confidence == IdentificationConfidence.LOW) {
                        _uiState.update {
                            it.copy(
                                isEvaluatingHealth = false,
                                pendingHealthEvaluation = result
                            )
                        }
                    } else {
                        applyHealthEvaluation(result)
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isEvaluatingHealth = false,
                            error = "Error al evaluar: ${e.message}"
                        )
                    }
                }
            )
        }
    }

    fun confirmPendingHealthEvaluation(apply: Boolean) {
        val pending = _uiState.value.pendingHealthEvaluation ?: return
        if (apply) {
            applyHealthEvaluation(pending)
        } else {
            _uiState.update {
                it.copy(
                    healthEvaluation = pending,
                    pendingHealthEvaluation = null
                )
            }
        }
    }

    private fun applyHealthEvaluation(result: PlantHealthEvaluationResult) {
        val plant = _uiState.value.plant ?: return
        viewModelScope.launch {
            val updatedPlant = plant.copy(status = result.suggestedStatus)
            updatePlant(updatedPlant).fold(
                onSuccess = {
                    val historyNotes = buildString {
                        append("Evaluación IA: ")
                        append(result.summary)
                        if (result.recommendedActions.isNotEmpty()) {
                            append("\nAcciones: ")
                            append(result.recommendedActions.joinToString("; "))
                        }
                    }
                    registerMaintenance(
                        plantId = plant.id,
                        type = ReminderType.CUSTOM,
                        action = MaintenanceAction.DONE,
                        notes = historyNotes
                    )
                    _uiState.update {
                        it.copy(
                            plant = updatedPlant,
                            healthEvaluation = result,
                            pendingHealthEvaluation = null,
                            isEvaluatingHealth = false
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isEvaluatingHealth = false,
                            error = "Error al guardar estado: ${e.message}"
                        )
                    }
                }
            )
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

    private fun fetchAndCacheCuriosities(plant: Plant) {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingCuriosities = true) }
            val name = plant.scientificName?.takeIf { it.isNotBlank() } ?: plant.commonName
            getPlantCuriosities(name).fold(
                onSuccess = { text ->
                    _uiState.update { it.copy(curiosities = text, loadingCuriosities = false) }
                    updatePlant(plant.copy(aiCuriosities = text))
                },
                onFailure = {
                    _uiState.update { it.copy(loadingCuriosities = false) }
                }
            )
        }
    }

    fun deletePlant() {
        val plant = _uiState.value.plant ?: return
        viewModelScope.launch {
            deletePlant(plant.id)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
