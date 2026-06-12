package com.verdy.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.verdy.data.local.db.converter.Converters
import com.verdy.data.local.db.dao.MaintenanceLogDao
import com.verdy.data.local.db.dao.PlantDao
import com.verdy.data.local.db.dao.ReminderDao
import com.verdy.data.local.db.entity.MaintenanceLogEntity
import com.verdy.data.local.db.entity.PlantEntity
import com.verdy.data.local.db.entity.ReminderEntity

@Database(
    entities = [
        PlantEntity::class,
        ReminderEntity::class,
        MaintenanceLogEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class VerdyDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun reminderDao(): ReminderDao
    abstract fun maintenanceLogDao(): MaintenanceLogDao

    companion object {
        const val DATABASE_NAME = "verdy_database"
    }
}
