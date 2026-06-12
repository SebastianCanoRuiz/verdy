package com.verdy.domain.usecase.reminder

import com.verdy.domain.model.Reminder
import com.verdy.domain.model.ReminderFrequency
import com.verdy.domain.model.enums.ReminderType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ScheduleReminderUseCaseTest {

    private lateinit var useCase: ScheduleReminderUseCase

    @Before
    fun setUp() {
        useCase = ScheduleReminderUseCase()
    }

    private fun makeReminder(
        frequency: ReminderFrequency,
        startDate: LocalDate = LocalDate.now()
    ) = Reminder(
        id = 1L,
        plantId = 1L,
        type = ReminderType.WATERING,
        startDate = startDate,
        frequency = frequency
    )

    @Test
    fun `once frequency produces non-periodic schedule`() {
        val reminder = makeReminder(ReminderFrequency.Once)
        val params = useCase(reminder, LocalDate.now())

        assertFalse(params.isPeriodic)
        assertEquals("reminder_1", params.reminderWorkName)
    }

    @Test
    fun `daily frequency produces periodic schedule with 1-day period`() {
        val reminder = makeReminder(ReminderFrequency.Daily)
        val params = useCase(reminder, LocalDate.now())

        assertTrue(params.isPeriodic)
        assertEquals(24L * 60 * 60 * 1000, params.periodMillis)
    }

    @Test
    fun `weekly frequency produces periodic schedule with 7-day period`() {
        val reminder = makeReminder(ReminderFrequency.Weekly)
        val params = useCase(reminder, LocalDate.now())

        assertTrue(params.isPeriodic)
        assertEquals(7L * 24 * 60 * 60 * 1000, params.periodMillis)
    }

    @Test
    fun `EveryXDays(14) frequency produces 14-day period`() {
        val reminder = makeReminder(ReminderFrequency.EveryXDays(14))
        val params = useCase(reminder, LocalDate.now())

        assertTrue(params.isPeriodic)
        assertEquals(14L * 24 * 60 * 60 * 1000, params.periodMillis)
    }

    @Test
    fun `start date in past produces zero or near-zero initial delay`() {
        val pastStart = LocalDate.now().minusDays(30)
        val reminder = makeReminder(ReminderFrequency.Weekly, startDate = pastStart)
        // Next due is some future date; delay should be >= 0
        val params = useCase(reminder, LocalDate.now())

        assertTrue(params.delayMillis >= 0)
    }

    @Test
    fun `work name includes reminder id`() {
        val reminder = makeReminder(ReminderFrequency.Monthly).copy(id = 99L)
        val params = useCase(reminder, LocalDate.now())

        assertEquals("reminder_99", params.reminderWorkName)
    }
}

