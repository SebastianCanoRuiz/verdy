package com.verdy.domain.usecase.reminder

import com.verdy.domain.repository.ReminderRepository
import javax.inject.Inject

class ToggleReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(reminderId: Long): Result<Unit> = runCatching {
        val reminder = reminderRepository.getReminderById(reminderId)
            ?: error("Recordatorio no encontrado")
        reminderRepository.updateReminder(reminder.copy(isActive = !reminder.isActive))
    }
}
