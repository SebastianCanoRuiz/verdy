package com.verdy.domain.model

import com.verdy.domain.model.enums.ReminderType
import java.time.LocalDate

/**
 * Represents a scheduled care reminder for a plant.
 * A reminder drives both WorkManager scheduling and notification display.
 */
data class Reminder(
    val id: Long = 0,
    val plantId: Long,
    val type: ReminderType,
    val customLabel: String? = null,
    val startDate: LocalDate,
    val frequency: ReminderFrequency,
    val isActive: Boolean = true,
    /** WorkManager unique work name for this reminder, used to cancel/reschedule */
    val workManagerId: String? = null
) {
    /** The label shown to users: custom label or type name */
    val displayLabel: String get() = customLabel ?: type.name

    /** Next due date relative to today */
    fun nextDueDate(referenceDate: LocalDate = LocalDate.now()): LocalDate =
        frequency.nextDueDate(startDate, referenceDate)

    /**
     * Returns true if this reminder is due today OR is overdue (past due and not yet completed).
     * Uses startDate as the base: the first occurrence is startDate + period.
     * If that date is today or already passed, the reminder is pending.
     */
    fun isDueToday(today: LocalDate = LocalDate.now()): Boolean {
        if (!isActive) return false
        val firstDueAfterStart = frequency.nextDueDate(startDate, startDate.plusDays(1))
        return !firstDueAfterStart.isAfter(today)
    }

    /** Returns true if this reminder is due within [days] days from [today] */
    fun isDueWithin(days: Int, today: LocalDate = LocalDate.now()): Boolean {
        if (!isActive) return false
        val next = nextDueDate(today)
        return !next.isAfter(today.plusDays(days.toLong()))
    }
}
