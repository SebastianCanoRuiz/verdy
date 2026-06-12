package com.verdy.data.repository

import com.verdy.data.local.db.dao.MaintenanceLogDao
import com.verdy.data.local.db.entity.MaintenanceLogEntity
import com.verdy.domain.model.MaintenanceLog
import com.verdy.domain.model.enums.MaintenanceAction
import com.verdy.domain.model.enums.ReminderType
import com.verdy.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class MaintenanceRepositoryImpl @Inject constructor(
    private val maintenanceLogDao: MaintenanceLogDao
) : MaintenanceRepository {

    override fun getMaintenanceHistoryForPlant(plantId: Long): Flow<List<MaintenanceLog>> =
        maintenanceLogDao.getMaintenanceHistoryForPlant(plantId)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun getLastCareDate(plantId: Long, type: ReminderType): LocalDate? =
        maintenanceLogDao.getLastLogForPlantAndType(
            plantId = plantId,
            type = type.name,
            action = MaintenanceAction.DONE.name
        )?.date

    override suspend fun addMaintenanceLog(log: MaintenanceLog): Long =
        maintenanceLogDao.insertLog(MaintenanceLogEntity.fromDomain(log))

    override suspend fun deleteMaintenanceLogsForPlant(plantId: Long) =
        maintenanceLogDao.deleteLogsForPlant(plantId)
}
