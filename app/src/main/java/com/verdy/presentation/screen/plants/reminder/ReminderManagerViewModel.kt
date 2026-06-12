package com.verdy.presentation.screen.plants.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verdy.data.worker.WorkManagerScheduler
import com.verdy.domain.model.Reminder
import com.verdy.domain.usecase.reminder.AddReminderUseCase
import com.verdy.domain.usecase.reminder.ToggleReminderUseCase
import com.verdy.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderManagerUiState(
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ReminderManagerViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val addReminderUseCase: AddReminderUseCase,
    private val toggleReminderUseCase: ToggleReminderUseCase,
    private val workManagerScheduler: WorkManagerScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderManagerUiState())
    val uiState: StateFlow<ReminderManagerUiState> = _uiState

    fun loadReminders(plantId: Long) {
        viewModelScope.launch {
            reminderRepository.getRemindersByPlantId(plantId).collect { reminders ->
                _uiState.update { it.copy(reminders = reminders, isLoading = false) }
            }
        }
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            val result = addReminderUseCase(reminder)
            result.onSuccess { id ->
                val savedReminder = reminder.copy(id = id)
                if (savedReminder.isActive) {
                    workManagerScheduler.schedule(savedReminder)
                }
            }
        }
    }

    fun toggleReminder(reminderId: Long) {
        viewModelScope.launch {
            toggleReminderUseCase(reminderId)
            val reminder = reminderRepository.getReminderById(reminderId)
            if (reminder != null) {
                if (reminder.isActive) workManagerScheduler.schedule(reminder)
                else workManagerScheduler.cancel(reminder)
            }
        }
    }

    fun deleteReminder(reminderId: Long) {
        viewModelScope.launch {
            val reminder = reminderRepository.getReminderById(reminderId)
            reminder?.let { workManagerScheduler.cancel(it) }
            reminderRepository.deleteReminder(reminderId)
        }
    }
}
