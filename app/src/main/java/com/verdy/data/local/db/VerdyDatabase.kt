package com.verdy.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.verdy.data.local.db.converter.Converters
import com.verdy.data.local.db.dao.MaintenanceLogDao
import com.verdy.data.local.db.dao.PlantDao
import com.verdy.data.local.db.dao.PlantEnvironmentDao
import com.verdy.data.local.db.dao.ReminderDao
import com.verdy.data.local.db.entity.MaintenanceLogEntity
import com.verdy.data.local.db.entity.PlantEntity
import com.verdy.data.local.db.entity.PlantEnvironmentEntity
import com.verdy.data.local.db.entity.ReminderEntity
import com.verdy.data.local.db.migration.MIGRATION_1_2
import com.verdy.data.local.db.migration.MIGRATION_2_3

@Database(
    entities = [
        PlantEntity::class,
        PlantEnvironmentEntity::class,
        ReminderEntity::class,
        MaintenanceLogEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class VerdyDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun plantEnvironmentDao(): PlantEnvironmentDao
    abstract fun reminderDao(): ReminderDao
    abstract fun maintenanceLogDao(): MaintenanceLogDao

    companion object {
        const val DATABASE_NAME = "verdy_database"
        val MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
    }
}
