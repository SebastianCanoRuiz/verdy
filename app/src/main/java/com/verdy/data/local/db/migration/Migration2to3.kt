package com.verdy.data.local.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS plant_environments (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                sort_order INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_plant_environments_name ON plant_environments(name)"
        )

        val defaults = listOf(
            "Sala", "Habitación", "Cocina", "Baño", "Balcón", "Patio", "Jardín"
        )
        defaults.forEachIndexed { index, name ->
            db.execSQL(
                "INSERT INTO plant_environments (name, sort_order) VALUES (?, ?)",
                arrayOf(name, index)
            )
        }

        db.execSQL("ALTER TABLE plants ADD COLUMN environment_id INTEGER")

        val plantsCursor = db.query("SELECT id, location FROM plants WHERE location IS NOT NULL AND TRIM(location) != ''")
        val envMap = mutableMapOf<String, Long>()
        db.query("SELECT id, name FROM plant_environments").use { envCursor ->
            while (envCursor.moveToNext()) {
                val envId = envCursor.getLong(0)
                val envName = envCursor.getString(1)?.trim()?.lowercase() ?: continue
                envMap[envName] = envId
            }
        }
        while (plantsCursor.moveToNext()) {
            val plantId = plantsCursor.getLong(0)
            val location = plantsCursor.getString(1)?.trim()?.lowercase() ?: continue
            envMap[location]?.let { envId ->
                db.execSQL(
                    "UPDATE plants SET environment_id = ? WHERE id = ?",
                    arrayOf(envId, plantId)
                )
            }
        }
        plantsCursor.close()
    }
}
