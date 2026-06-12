package com.verdy.data.repository

import com.verdy.data.local.db.dao.ReminderDao
import com.verdy.data.local.db.entity.ReminderEntity
import com.verdy.domain.model.Reminder
import com.verdy.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun getAllReminders(): Flow<List<Reminder>> =
        reminderDao.getAllReminders().map { entities -> entities.map { it.toDomain() } }

    override fun getRemindersByPlantId(plantId: Long): Flow<List<Reminder>> =
        reminderDao.getRemindersByPlantId(plantId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getReminderById(id: Long): Reminder? =
        reminderDao.getReminderById(id)?.toDomain()

    override suspend fun addReminder(reminder: Reminder): Long =
        reminderDao.insertReminder(ReminderEntity.fromDomain(reminder))

    override suspend fun updateReminder(reminder: Reminder) =
        reminderDao.updateReminder(ReminderEntity.fromDomain(reminder))

    override suspend fun deleteReminder(id: Long) =
        reminderDao.deleteReminder(id)

    override suspend fun deleteRemindersByPlantId(plantId: Long) =
        reminderDao.deleteRemindersByPlantId(plantId)

    override suspend fun getAllRemindersSnapshot(): List<Reminder> =
        reminderDao.getAllRemindersSnapshot().map { it.toDomain() }
}
