package com.verdy.domain.repository

import com.verdy.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getAllReminders(): Flow<List<Reminder>>
    fun getRemindersByPlantId(plantId: Long): Flow<List<Reminder>>
    suspend fun getReminderById(id: Long): Reminder?
    suspend fun addReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(id: Long)
    suspend fun deleteRemindersByPlantId(plantId: Long)
    suspend fun getAllRemindersSnapshot(): List<Reminder>
}
