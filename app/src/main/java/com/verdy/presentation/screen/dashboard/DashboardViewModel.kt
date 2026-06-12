package com.verdy.presentation.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verdy.domain.model.Plant
import com.verdy.domain.model.Reminder
import com.verdy.domain.model.enums.MaintenanceAction
import com.verdy.domain.repository.PlantRepository
import com.verdy.domain.usecase.maintenance.RegisterMaintenanceUseCase
import com.verdy.domain.usecase.reminder.GetTodayRemindersUseCase
import com.verdy.domain.usecase.reminder.GetUpcomingRemindersUseCase
import com.verdy.domain.usecase.reminder.UpdateReminderUseCase
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
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val plantRepository: PlantRepository,
    private val getTodayReminders: GetTodayRemindersUseCase,
    private val getUpcomingReminders: GetUpcomingRemindersUseCase,
    private val registerMaintenance: RegisterMaintenanceUseCase,
    private val updateReminder: UpdateReminderUseCase
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

    fun refresh() {
        viewModelScope.launch {
            plantRepository.getAllPlants().collect { plantList ->
                refreshReminders(plantList)
                return@collect
            }
        }
    }
}
