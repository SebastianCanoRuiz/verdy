package com.verdy.data.local.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE plants ADD COLUMN medium TEXT NOT NULL DEFAULT 'SOIL'")
        db.execSQL("ALTER TABLE plants ADD COLUMN water_change_frequency_days INTEGER")
        db.execSQL("ALTER TABLE plants ADD COLUMN ai_curiosities TEXT")
    }
}
