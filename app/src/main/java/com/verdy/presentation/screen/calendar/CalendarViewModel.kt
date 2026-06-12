package com.verdy.presentation.screen.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verdy.domain.model.Plant
import com.verdy.domain.model.Reminder
import com.verdy.domain.repository.PlantRepository
import com.verdy.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class DayReminders(
    val date: LocalDate,
    val items: List<Pair<Plant, Reminder>>
)

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val daysWithReminders: Set<LocalDate> = emptySet(),
    val selectedDayItems: List<Pair<Plant, Reminder>> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val plantRepository: PlantRepository,
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                plantRepository.getAllPlants(),
                reminderRepository.getAllReminders()
            ) { plants, reminders ->
                Pair(plants, reminders)
            }.collect { (plants, reminders) ->
                val plantMap = plants.associateBy { it.id }
                val month = _uiState.value.currentMonth
                val daysInRange = buildDaysWithReminders(reminders, month)
                val selectedItems = buildItemsForDay(_uiState.value.selectedDate, reminders, plantMap)

                _uiState.update {
                    it.copy(
                        daysWithReminders = daysInRange,
                        selectedDayItems = selectedItems,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onMonthChange(month: YearMonth) {
        _uiState.update { it.copy(currentMonth = month) }
        refreshForMonth(month)
    }

    fun onDaySelected(date: LocalDate) {
        viewModelScope.launch {
            val reminders = reminderRepository.getAllRemindersSnapshot()
            val plants = plantRepository.getAllPlants()
            plants.collect { plantList ->
                val plantMap = plantList.associateBy { it.id }
                val items = buildItemsForDay(date, reminders, plantMap)
                _uiState.update {
                    it.copy(
                        selectedDate = date,
                        selectedDayItems = items
                    )
                }
                return@collect
            }
        }
    }

    private fun refreshForMonth(month: YearMonth) {
        viewModelScope.launch {
            val reminders = reminderRepository.getAllRemindersSnapshot()
            val days = buildDaysWithReminders(reminders, month)
            _uiState.update { it.copy(daysWithReminders = days) }
        }
    }

    private fun buildDaysWithReminders(
        reminders: List<Reminder>,
        month: YearMonth
    ): Set<LocalDate> {
        val result = mutableSetOf<LocalDate>()
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        reminders.filter { it.isActive }.forEach { reminder ->
            var date = reminder.nextDueDate(start)
            while (!date.isAfter(end)) {
                result.add(date)
                if (reminder.frequency.isOneTime) break
                date = reminder.nextDueDate(date.plusDays(1))
            }
        }
        return result
    }

    private fun buildItemsForDay(
        date: LocalDate,
        reminders: List<Reminder>,
        plantMap: Map<Long, Plant>
    ): List<Pair<Plant, Reminder>> {
        return reminders.filter { reminder ->
            reminder.isActive && reminder.nextDueDate(date) == date
        }.mapNotNull { reminder ->
            plantMap[reminder.plantId]?.let { plant -> plant to reminder }
        }
    }
}
