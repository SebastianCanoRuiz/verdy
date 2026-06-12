package com.verdy.domain.usecase.reminder

import com.verdy.domain.model.Reminder
import com.verdy.domain.repository.ReminderRepository
import java.time.LocalDate
import javax.inject.Inject

/** Returns active reminders due within the next [daysAhead] days (excluding today) */
class GetUpcomingRemindersUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(
        daysAhead: Int = 7,
        today: LocalDate = LocalDate.now()
    ): List<Reminder> {
        return reminderRepository.getAllRemindersSnapshot()
            .filter { reminder ->
                if (!reminder.isActive) return@filter false
                val next = reminder.nextDueDate(today)
                next.isAfter(today) && !next.isAfter(today.plusDays(daysAhead.toLong()))
            }
            .sortedBy { it.nextDueDate(today) }
    }
}
