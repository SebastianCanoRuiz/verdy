package com.verdy.presentation.screen.plants.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verdy.data.worker.WorkManagerScheduler
import com.verdy.domain.model.CareInfo
import com.verdy.domain.model.Plant
import com.verdy.domain.model.Reminder
import com.verdy.domain.model.ReminderFrequency
import com.verdy.domain.model.enums.PlantMedium
import com.verdy.domain.model.enums.PlantStatus
import com.verdy.domain.model.enums.ReminderType
import com.verdy.domain.model.enums.SunExposure
import com.verdy.domain.usecase.ai.IdentifyPlantUseCase
import com.verdy.domain.usecase.plant.AddPlantUseCase
import com.verdy.presentation.screen.dashboard.IdentificationStore
import com.verdy.domain.usecase.plant.GetPlantByIdUseCase
import com.verdy.domain.usecase.plant.UpdatePlantUseCase
import com.verdy.domain.usecase.reminder.AddReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class PlantFormUiState(
    val customName: String = "",
    val commonName: String = "",
    val scientificName: String = "",
    val photoUri: String? = null,
    val acquisitionDate: LocalDate? = null,
    val location: String = "",
    val notes: String = "",
    val status: PlantStatus = PlantStatus.HEALTHY,
    val wateringFrequencyDays: String = "7",
    val fertilizingFrequencyDays: String = "",
    val fertilizerType: String = "",
    val waterAmountMl: String = "",
    val sunExposure: SunExposure = SunExposure.SEMI_SHADE,
    val medium: PlantMedium = PlantMedium.SOIL,
    val waterChangeFrequencyDays: String = "7",
    val autoCreateWateringReminder: Boolean = true,
    val autoCreateFertilizingReminder: Boolean = true,
    val autoCreateWaterChangeReminder: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isIdentifying: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false
)

@HiltViewModel
class PlantFormViewModel @Inject constructor(
    private val addPlant: AddPlantUseCase,
    private val updatePlant: UpdatePlantUseCase,
    private val getPlantById: GetPlantByIdUseCase,
    private val addReminder: AddReminderUseCase,
    private val workManagerScheduler: WorkManagerScheduler,
    private val identifyPlant: IdentifyPlantUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlantFormUiState())
    val uiState: StateFlow<PlantFormUiState> = _uiState

    private var editPlantId: Long? = null

    init {
        // Pre-fill form if an identification result is waiting
        IdentificationStore.consume()?.let { result ->
            _uiState.update { state ->
                state.copy(
                    commonName = result.commonName,
                    scientificName = result.scientificName,
                    sunExposure = result.sunExposure,
                    wateringFrequencyDays = result.wateringFrequencyDays.toString(),
                    notes = buildString {
                        if (result.curiosities.isNotBlank()) append(result.curiosities)
                        if (result.regions.isNotBlank()) append("\n\nRegiones: ${result.regions}")
                    }
                )
            }
        }
    }

    fun loadPlant(plantId: Long) {
        if (plantId <= 0) return
        editPlantId = plantId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val plant = getPlantById(plantId)
            if (plant != null) {
                _uiState.update {
                    it.copy(
                        customName = plant.customName,
                        commonName = plant.commonName,
                        scientificName = plant.scientificName ?: "",
                        photoUri = plant.photoUri,
                        acquisitionDate = plant.acquisitionDate,
                        location = plant.location ?: "",
                        notes = plant.notes ?: "",
                        status = plant.status,
                        wateringFrequencyDays = plant.careInfo.wateringFrequencyDays.toString(),
                        fertilizingFrequencyDays = plant.careInfo.fertilizingFrequencyDays?.toString() ?: "",
                        fertilizerType = plant.careInfo.fertilizerType ?: "",
                        waterAmountMl = plant.careInfo.waterAmountMl?.toString() ?: "",
                        sunExposure = plant.careInfo.sunExposure,
                        medium = plant.careInfo.medium,
                        waterChangeFrequencyDays = plant.careInfo.waterChangeFrequencyDays?.toString() ?: "7",
                        isLoading = false,
                        isEditMode = true
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onCustomNameChange(value: String) = _uiState.update { it.copy(customName = value) }
    fun onCommonNameChange(value: String) = _uiState.update { it.copy(commonName = value) }
    fun onScientificNameChange(value: String) = _uiState.update { it.copy(scientificName = value) }
    fun onPhotoUriChange(uri: String?) = _uiState.update { it.copy(photoUri = uri) }
    fun onAcquisitionDateChange(date: LocalDate?) = _uiState.update { it.copy(acquisitionDate = date) }
    fun onLocationChange(value: String) = _uiState.update { it.copy(location = value) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }
    fun onStatusChange(status: PlantStatus) = _uiState.update { it.copy(status = status) }
    fun onWateringFrequencyChange(value: String) = _uiState.update { it.copy(wateringFrequencyDays = value) }
    fun onFertilizingFrequencyChange(value: String) = _uiState.update { it.copy(fertilizingFrequencyDays = value) }
    fun onFertilizerTypeChange(value: String) = _uiState.update { it.copy(fertilizerType = value) }
    fun onWaterAmountChange(value: String) = _uiState.update { it.copy(waterAmountMl = value) }
    fun onSunExposureChange(value: SunExposure) = _uiState.update { it.copy(sunExposure = value) }
    fun onMediumChange(value: PlantMedium) = _uiState.update { it.copy(medium = value) }
    fun onWaterChangeFrequencyChange(value: String) = _uiState.update { it.copy(waterChangeFrequencyDays = value) }
    fun onAutoWateringReminderChange(value: Boolean) = _uiState.update { it.copy(autoCreateWateringReminder = value) }
    fun onAutoFertilizingReminderChange(value: Boolean) = _uiState.update { it.copy(autoCreateFertilizingReminder = value) }
    fun onAutoWaterChangeReminderChange(value: Boolean) = _uiState.update { it.copy(autoCreateWaterChangeReminder = value) }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val plant = Plant(
                id = editPlantId ?: 0L,
                customName = state.customName.trim(),
                commonName = state.commonName.trim(),
                scientificName = state.scientificName.trim().takeIf { it.isNotBlank() },
                photoUri = state.photoUri,
                acquisitionDate = state.acquisitionDate,
                location = state.location.trim().takeIf { it.isNotBlank() },
                notes = state.notes.trim().takeIf { it.isNotBlank() },
                status = state.status,
                careInfo = CareInfo(
                    wateringFrequencyDays = state.wateringFrequencyDays.toIntOrNull() ?: 7,
                    fertilizingFrequencyDays = state.fertilizingFrequencyDays.toIntOrNull(),
                    fertilizerType = state.fertilizerType.trim().takeIf { it.isNotBlank() },
                    waterAmountMl = state.waterAmountMl.toIntOrNull(),
                    sunExposure = state.sunExposure,
                    medium = state.medium,
                    waterChangeFrequencyDays = if (state.medium == PlantMedium.WATER)
                        state.waterChangeFrequencyDays.toIntOrNull() else null
                )
            )

            if (editPlantId != null) {
                updatePlant(plant).fold(
                    onSuccess = { _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) } },
                    onFailure = { e -> _uiState.update { it.copy(isSaving = false, error = e.message) } }
                )
            } else {
                addPlant(plant).fold(
                    onSuccess = { newPlantId ->
                        autoCreateReminders(newPlantId, state)
                        _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
                    },
                    onFailure = { e -> _uiState.update { it.copy(isSaving = false, error = e.message) } }
                )
            }
        }
    }

    fun identifyFromPhoto(imagePath: String) {
        if (imagePath.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isIdentifying = true, error = null) }
            identifyPlant(imagePath).fold(
                onSuccess = { result ->
                    _uiState.update { state ->
                        state.copy(
                            commonName = result.commonName,
                            scientificName = result.scientificName,
                            sunExposure = result.sunExposure,
                            wateringFrequencyDays = result.wateringFrequencyDays.toString(),
                            notes = buildString {
                                if (state.notes.isNotBlank()) append(state.notes).append("\n\n")
                                if (result.curiosities.isNotBlank()) append(result.curiosities)
                                if (result.regions.isNotBlank()) append("\n\nRegiones: ${result.regions}")
                            },
                            isIdentifying = false
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isIdentifying = false, error = "Error al identificar: ${e.message}") }
                }
            )
        }
    }

    private suspend fun autoCreateReminders(plantId: Long, state: PlantFormUiState) {
        val wateringDays = state.wateringFrequencyDays.toIntOrNull() ?: 0
        if (state.autoCreateWateringReminder && wateringDays > 0) {
            val reminder = Reminder(
                plantId = plantId,
                type = ReminderType.WATERING,
                startDate = LocalDate.now(),
                frequency = ReminderFrequency.EveryXDays(wateringDays)
            )
            addReminder(reminder).getOrNull()?.let { id ->
                workManagerScheduler.schedule(reminder.copy(id = id))
            }
        }

        val fertilizingDays = state.fertilizingFrequencyDays.toIntOrNull() ?: 0
        if (state.autoCreateFertilizingReminder && fertilizingDays > 0) {
            val reminder = Reminder(
                plantId = plantId,
                type = ReminderType.FERTILIZING,
                startDate = LocalDate.now(),
                frequency = ReminderFrequency.EveryXDays(fertilizingDays)
            )
            addReminder(reminder).getOrNull()?.let { id ->
                workManagerScheduler.schedule(reminder.copy(id = id))
            }
        }

        if (state.medium == PlantMedium.WATER) {
            val waterChangeDays = state.waterChangeFrequencyDays.toIntOrNull() ?: 0
            if (state.autoCreateWaterChangeReminder && waterChangeDays > 0) {
                val reminder = Reminder(
                    plantId = plantId,
                    type = ReminderType.WATER_CHANGE,
                    startDate = LocalDate.now(),
                    frequency = ReminderFrequency.EveryXDays(waterChangeDays)
                )
                addReminder(reminder).getOrNull()?.let { id ->
                    workManagerScheduler.schedule(reminder.copy(id = id))
                }
            }
        }
    }
}
