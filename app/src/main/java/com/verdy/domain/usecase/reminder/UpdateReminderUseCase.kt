package com.verdy.domain.usecase.reminder

import com.verdy.domain.model.Reminder
import com.verdy.domain.repository.ReminderRepository
import javax.inject.Inject

class UpdateReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(reminder: Reminder): Result<Unit> = runCatching {
        require(reminder.id > 0) { "ID de recordatorio inválido" }
        reminderRepository.updateReminder(reminder)
    }
}
