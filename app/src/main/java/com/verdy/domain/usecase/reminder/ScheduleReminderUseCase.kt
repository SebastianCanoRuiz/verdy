package com.verdy.domain.usecase.reminder

import com.verdy.domain.model.Reminder
import com.verdy.domain.model.ReminderFrequency
import javax.inject.Inject
import java.time.LocalDate
import java.time.ZoneId

/**
 * Calculates the initial delay in milliseconds for a WorkManager request
 * based on the reminder's next due date.
 */
class ScheduleReminderUseCase @Inject constructor() {

    data class ScheduleParams(
        val reminderWorkName: String,
        val delayMillis: Long,
        val isPeriodic: Boolean,
        val periodMillis: Long
    )

    operator fun invoke(reminder: Reminder, today: LocalDate = LocalDate.now()): ScheduleParams {
        val workName = "reminder_${reminder.id}"
        val nextDue = reminder.nextDueDate(today)

        val nextDueMillis = nextDue
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val nowMillis = System.currentTimeMillis()
        val delayMillis = maxOf(0L, nextDueMillis - nowMillis)

        val (isPeriodic, periodMillis) = when (val freq = reminder.frequency) {
            is ReminderFrequency.Once -> false to 0L
            is ReminderFrequency.Daily -> true to MILLIS_PER_DAY
            is ReminderFrequency.Weekly -> true to (MILLIS_PER_DAY * 7)
            is ReminderFrequency.EveryXDays -> true to (MILLIS_PER_DAY * freq.days)
            is ReminderFrequency.Monthly -> true to (MILLIS_PER_DAY * 30)
        }

        return ScheduleParams(workName, delayMillis, isPeriodic, periodMillis)
    }

    private companion object {
        const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    }
}
