package com.verdy.domain.usecase.reminder

import com.verdy.domain.model.Reminder
import com.verdy.domain.repository.ReminderRepository
import java.time.LocalDate
import javax.inject.Inject

/** Returns all active reminders whose next due date is today or overdue */
class GetTodayRemindersUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(today: LocalDate = LocalDate.now()): List<Reminder> {
        return reminderRepository.getAllRemindersSnapshot()
            .filter { it.isActive && it.isDueToday(today) }
    }
}
