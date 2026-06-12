package com.verdy.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.verdy.data.local.db.entity.MaintenanceLogEntity
import com.verdy.domain.model.enums.MaintenanceAction
import com.verdy.domain.model.enums.ReminderType
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceLogDao {

    @Query("SELECT * FROM maintenance_logs WHERE plant_id = :plantId ORDER BY date DESC")
    fun getMaintenanceHistoryForPlant(plantId: Long): Flow<List<MaintenanceLogEntity>>

    @Query("""
        SELECT * FROM maintenance_logs 
        WHERE plant_id = :plantId 
        AND type = :type 
        AND action = :action
        ORDER BY date DESC 
        LIMIT 1
    """)
    suspend fun getLastLogForPlantAndType(
        plantId: Long,
        type: String,
        action: String = MaintenanceAction.DONE.name
    ): MaintenanceLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MaintenanceLogEntity): Long

    @Query("DELETE FROM maintenance_logs WHERE plant_id = :plantId")
    suspend fun deleteLogsForPlant(plantId: Long)
}
