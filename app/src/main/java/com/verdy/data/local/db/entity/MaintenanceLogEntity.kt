package com.verdy.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.verdy.domain.model.MaintenanceLog
import com.verdy.domain.model.enums.MaintenanceAction
import com.verdy.domain.model.enums.ReminderType
import java.time.LocalDate

@Entity(
    tableName = "maintenance_logs",
    foreignKeys = [
        ForeignKey(
            entity = PlantEntity::class,
            parentColumns = ["id"],
            childColumns = ["plant_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plant_id")]
)
data class MaintenanceLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    @ColumnInfo(name = "plant_id") val plantId: Long,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "type") val type: ReminderType,
    @ColumnInfo(name = "action") val action: MaintenanceAction,
    @ColumnInfo(name = "notes") val notes: String?
) {
    fun toDomain(): MaintenanceLog = MaintenanceLog(
        id = id,
        plantId = plantId,
        date = date,
        type = type,
        action = action,
        notes = notes
    )

    companion object {
        fun fromDomain(log: MaintenanceLog): MaintenanceLogEntity = MaintenanceLogEntity(
            id = log.id,
            plantId = log.plantId,
            date = log.date,
            type = log.type,
            action = log.action,
            notes = log.notes
        )
    }
}
