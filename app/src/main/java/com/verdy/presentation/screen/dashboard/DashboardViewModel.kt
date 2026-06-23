package com.verdy.presentation.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.verdy.domain.model.Plant
import com.verdy.domain.model.PlantIdentificationResult
import com.verdy.domain.model.Reminder
import com.verdy.domain.model.enums.MaintenanceAction
import com.verdy.domain.repository.PlantRepository
import com.verdy.domain.usecase.ai.IdentifyPlantUseCase
import com.verdy.domain.usecase.maintenance.RegisterMaintenanceUseCase
import com.verdy.domain.usecase.reminder.GetTodayRemindersUseCase
import com.verdy.domain.usecase.reminder.GetUpcomingRemindersUseCase
import com.verdy.domain.usecase.reminder.UpdateReminderUseCase
import com.verdy.domain.util.MoonPhase
import com.verdy.domain.util.MoonPhaseCalculator
import java.io.File
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class PlantReminderItem(
    val plant: Plant,
    val reminder: Reminder
)

data class DashboardUiState(
    val todayItems: List<PlantReminderItem> = emptyList(),
    val upcomingItems: List<PlantReminderItem> = emptyList(),
    val allPlants: List<Plant> = emptyList(),
    val totalPlants: Int = 0,
    val moonPhase: MoonPhase = MoonPhaseCalculator.currentPhase(),
    val isIdentifying: Boolean = false,
    val identificationResult: PlantIdentificationResult? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val plantRepository: PlantRepository,
    private val getTodayReminders: GetTodayRemindersUseCase,
    private val getUpcomingReminders: GetUpcomingRemindersUseCase,
    private val registerMaintenance: RegisterMaintenanceUseCase,
    private val updateReminder: UpdateReminderUseCase,
    private val identifyPlantUseCase: IdentifyPlantUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        viewModelScope.launch {
            plantRepository.getAllPlants().collect { plants ->
                refreshReminders(plants)
            }
        }
    }

    private suspend fun refreshReminders(plants: List<Plant>) {
        val today = LocalDate.now()
        val todayReminders = getTodayReminders(today)
        val upcomingReminders = getUpcomingReminders(7, today)

        val plantMap = plants.associateBy { it.id }

        val todayItems = todayReminders.mapNotNull { reminder ->
            plantMap[reminder.plantId]?.let { plant -> PlantReminderItem(plant, reminder) }
        }
        val upcomingItems = upcomingReminders.mapNotNull { reminder ->
            plantMap[reminder.plantId]?.let { plant -> PlantReminderItem(plant, reminder) }
        }.filter { item -> todayItems.none { it.reminder.id == item.reminder.id } }

        _uiState.update {
            it.copy(
                todayItems = todayItems,
                upcomingItems = upcomingItems,
                allPlants = plants,
                totalPlants = plants.size,
                isLoading = false
            )
        }
    }

    fun markAsDone(reminder: Reminder) {
        viewModelScope.launch {
            val today = LocalDate.now()
            registerMaintenance(
                plantId = reminder.plantId,
                type = reminder.type,
                action = MaintenanceAction.DONE
            )
            // Move startDate to today so the next occurrence is today + frequency
            updateReminder(reminder.copy(startDate = today))
            refresh()
        }
    }

    fun postpone(reminder: Reminder) {
        viewModelScope.launch {
            registerMaintenance(
                plantId = reminder.plantId,
                type = reminder.type,
                action = MaintenanceAction.POSTPONED
            )
            refresh()
        }
    }

    fun identifyPlant(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isIdentifying = true, identificationResult = null) }
            val imagePath = copyUriToTemp(context, uri) ?: run {
                _uiState.update { it.copy(isIdentifying = false, error = "No se pudo leer la imagen") }
                return@launch
            }
            identifyPlantUseCase(imagePath).fold(
                onSuccess = { result ->
                    _uiState.update { it.copy(isIdentifying = false, identificationResult = result) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isIdentifying = false, error = "Error al identificar: ${e.message}") }
                }
            )
            File(imagePath).delete()
        }
    }

    fun clearIdentificationResult() {
        _uiState.update { it.copy(identificationResult = null) }
    }

    private fun copyUriToTemp(context: Context, uri: Uri): String? =
        runCatching {
            val tmp = File(context.cacheDir, "identify_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { inp ->
                tmp.outputStream().use { out -> inp.copyTo(out) }
            }
            tmp.absolutePath
        }.getOrNull()

    fun refresh() {
        viewModelScope.launch {
            plantRepository.getAllPlants().collect { plantList ->
                refreshReminders(plantList)
                return@collect
            }
        }
    }
}
