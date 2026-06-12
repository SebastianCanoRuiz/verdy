package com.verdy.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.verdy.domain.model.Reminder
import com.verdy.domain.model.ReminderFrequency
import com.verdy.domain.model.enums.ReminderType
import java.time.LocalDate

@Entity(
    tableName = "reminders",
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
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    @ColumnInfo(name = "plant_id") val plantId: Long,
    @ColumnInfo(name = "type") val type: ReminderType,
    @ColumnInfo(name = "custom_label") val customLabel: String?,
    @ColumnInfo(name = "start_date") val startDate: LocalDate,

    // ReminderFrequency stored as type + optional value
    @ColumnInfo(name = "frequency_type") val frequencyType: String,
    @ColumnInfo(name = "frequency_value") val frequencyValue: Int?,

    @ColumnInfo(name = "is_active") val isActive: Boolean,
    @ColumnInfo(name = "work_manager_id") val workManagerId: String?
) {
    fun toDomain(): Reminder = Reminder(
        id = id,
        plantId = plantId,
        type = type,
        customLabel = customLabel,
        startDate = startDate,
        frequency = frequencyFromEntity(frequencyType, frequencyValue),
        isActive = isActive,
        workManagerId = workManagerId
    )

    companion object {
        fun fromDomain(reminder: Reminder): ReminderEntity {
            val (freqType, freqValue) = frequencyToEntity(reminder.frequency)
            return ReminderEntity(
                id = reminder.id,
                plantId = reminder.plantId,
                type = reminder.type,
                customLabel = reminder.customLabel,
                startDate = reminder.startDate,
                frequencyType = freqType,
                frequencyValue = freqValue,
                isActive = reminder.isActive,
                workManagerId = reminder.workManagerId
            )
        }

        private fun frequencyToEntity(freq: ReminderFrequency): Pair<String, Int?> = when (freq) {
            is ReminderFrequency.Once -> "ONCE" to null
            is ReminderFrequency.Daily -> "DAILY" to null
            is ReminderFrequency.Weekly -> "WEEKLY" to null
            is ReminderFrequency.EveryXDays -> "EVERY_X_DAYS" to freq.days
            is ReminderFrequency.Monthly -> "MONTHLY" to null
        }

        private fun frequencyFromEntity(type: String, value: Int?): ReminderFrequency = when (type) {
            "ONCE" -> ReminderFrequency.Once
            "DAILY" -> ReminderFrequency.Daily
            "WEEKLY" -> ReminderFrequency.Weekly
            "EVERY_X_DAYS" -> ReminderFrequency.EveryXDays(value ?: 7)
            "MONTHLY" -> ReminderFrequency.Monthly
            else -> ReminderFrequency.Weekly
        }
    }
}
