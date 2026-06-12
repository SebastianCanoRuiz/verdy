package com.verdy.data.local.db.converter

import androidx.room.TypeConverter
import com.verdy.domain.model.enums.MaintenanceAction
import com.verdy.domain.model.enums.PlantStatus
import com.verdy.domain.model.enums.ReminderType
import com.verdy.domain.model.enums.SunExposure
import java.time.LocalDate

class Converters {

    // LocalDate <-> Long (epoch day)
    @TypeConverter fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()
    @TypeConverter fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let { LocalDate.ofEpochDay(it) }

    // PlantStatus
    @TypeConverter fun fromPlantStatus(value: PlantStatus): String = value.name
    @TypeConverter fun toPlantStatus(value: String): PlantStatus = PlantStatus.valueOf(value)

    // SunExposure
    @TypeConverter fun fromSunExposure(value: SunExposure): String = value.name
    @TypeConverter fun toSunExposure(value: String): SunExposure =
        runCatching { SunExposure.valueOf(value) }.getOrDefault(SunExposure.SEMI_SHADE)

    // ReminderType
    @TypeConverter fun fromReminderType(value: ReminderType): String = value.name
    @TypeConverter fun toReminderType(value: String): ReminderType = ReminderType.valueOf(value)

    // MaintenanceAction
    @TypeConverter fun fromMaintenanceAction(value: MaintenanceAction): String = value.name
    @TypeConverter fun toMaintenanceAction(value: String): MaintenanceAction = MaintenanceAction.valueOf(value)
}
