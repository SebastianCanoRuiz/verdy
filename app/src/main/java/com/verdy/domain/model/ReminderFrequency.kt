package com.verdy.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Defines how often a reminder repeats.
 * Used to calculate the next due date of any care action.
 */
sealed class ReminderFrequency {
    object Once : ReminderFrequency()
    object Daily : ReminderFrequency()
    object Weekly : ReminderFrequency()
    data class EveryXDays(val days: Int) : ReminderFrequency()
    object Monthly : ReminderFrequency()

    /** Human-readable label key matching strings.xml entries */
    val labelKey: String get() = when (this) {
        is Once -> "freq_once"
        is Daily -> "freq_daily"
        is Weekly -> "freq_weekly"
        is EveryXDays -> "freq_every_x_days"
        is Monthly -> "freq_monthly"
    }

    /**
     * Calculates the next date this reminder is due on or after [referenceDate].
     * Starts from [startDate] and advances by the configured period.
     */
    fun nextDueDate(startDate: LocalDate, referenceDate: LocalDate = LocalDate.now()): LocalDate {
        if (!startDate.isBefore(referenceDate)) return startDate
        return when (this) {
            is Once -> startDate
            is Daily -> {
                val daysBetween = ChronoUnit.DAYS.between(startDate, referenceDate)
                startDate.plusDays(daysBetween + if (startDate.plusDays(daysBetween) < referenceDate) 1 else 0)
            }
            is Weekly -> {
                val weeksBetween = ChronoUnit.WEEKS.between(startDate, referenceDate)
                val candidate = startDate.plusWeeks(weeksBetween)
                if (candidate.isBefore(referenceDate)) candidate.plusWeeks(1) else candidate
            }
            is EveryXDays -> {
                val daysBetween = ChronoUnit.DAYS.between(startDate, referenceDate)
                val periods = daysBetween / days
                val candidate = startDate.plusDays(periods * days)
                if (candidate.isBefore(referenceDate)) candidate.plusDays(days.toLong()) else candidate
            }
            is Monthly -> {
                val monthsBetween = ChronoUnit.MONTHS.between(startDate, referenceDate)
                val candidate = startDate.plusMonths(monthsBetween)
                if (candidate.isBefore(referenceDate)) candidate.plusMonths(1) else candidate
            }
        }
    }

    /** Returns true if this frequency generates only a single reminder */
    val isOneTime: Boolean get() = this is Once
}
