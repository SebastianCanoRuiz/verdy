package com.verdy.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ReminderFrequencyTest {

    private val start = LocalDate.of(2025, 1, 1)

    @Test
    fun `Once always returns start date`() {
        val freq = ReminderFrequency.Once
        assertEquals(start, freq.nextDueDate(start, LocalDate.of(2025, 6, 1)))
    }

    @Test
    fun `Daily next due from same day is start date`() {
        val freq = ReminderFrequency.Daily
        assertEquals(start, freq.nextDueDate(start, start))
    }

    @Test
    fun `Daily next due from day after start is next day`() {
        val freq = ReminderFrequency.Daily
        val reference = start.plusDays(1)
        assertEquals(reference, freq.nextDueDate(start, reference))
    }

    @Test
    fun `Weekly next due from 8 days after start is 14 days after start`() {
        val freq = ReminderFrequency.Weekly
        val reference = start.plusDays(8)
        val expected = start.plusWeeks(2)
        assertEquals(expected, freq.nextDueDate(start, reference))
    }

    @Test
    fun `EveryXDays(10) next due from day 11 is day 20`() {
        val freq = ReminderFrequency.EveryXDays(10)
        val reference = start.plusDays(11)
        val expected = start.plusDays(20)
        assertEquals(expected, freq.nextDueDate(start, reference))
    }

    @Test
    fun `Monthly next due from same day is start`() {
        val freq = ReminderFrequency.Monthly
        assertEquals(start, freq.nextDueDate(start, start))
    }

    @Test
    fun `Monthly next due from 32 days after is 2 months after`() {
        val freq = ReminderFrequency.Monthly
        val reference = start.plusDays(32)
        val expected = start.plusMonths(2)
        assertEquals(expected, freq.nextDueDate(start, reference))
    }

    @Test
    fun `Once isOneTime is true`() {
        assertTrue(ReminderFrequency.Once.isOneTime)
    }

    @Test
    fun `Weekly isOneTime is false`() {
        assertFalse(ReminderFrequency.Weekly.isOneTime)
    }

    private fun assertTrue(value: Boolean) = org.junit.Assert.assertTrue(value)
    private fun assertFalse(value: Boolean) = org.junit.Assert.assertFalse(value)
}
