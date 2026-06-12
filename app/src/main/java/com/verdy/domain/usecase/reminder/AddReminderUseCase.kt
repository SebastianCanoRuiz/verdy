package com.verdy.domain.usecase.reminder

import com.verdy.domain.model.Reminder
import com.verdy.domain.repository.ReminderRepository
import javax.inject.Inject

class AddReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(reminder: Reminder): Result<Long> = runCatching {
        require(reminder.plantId > 0) { "ID de planta inválido" }
        reminderRepository.addReminder(reminder)
    }
}
