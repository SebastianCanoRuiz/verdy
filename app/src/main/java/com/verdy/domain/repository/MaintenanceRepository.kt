package com.verdy.domain.repository

import com.verdy.domain.model.MaintenanceLog
import com.verdy.domain.model.enums.ReminderType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MaintenanceRepository {
    fun getMaintenanceHistoryForPlant(plantId: Long): Flow<List<MaintenanceLog>>
    suspend fun getLastCareDate(plantId: Long, type: ReminderType): LocalDate?
    suspend fun addMaintenanceLog(log: MaintenanceLog): Long
    suspend fun deleteMaintenanceLogsForPlant(plantId: Long)
}
