package com.verdy.di

import android.content.Context
import androidx.room.Room
import com.verdy.data.local.db.VerdyDatabase
import com.verdy.data.local.db.dao.MaintenanceLogDao
import com.verdy.data.local.db.dao.PlantDao
import com.verdy.data.local.db.dao.ReminderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VerdyDatabase =
        Room.databaseBuilder(
            context,
            VerdyDatabase::class.java,
            VerdyDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePlantDao(db: VerdyDatabase): PlantDao = db.plantDao()

    @Provides
    fun provideReminderDao(db: VerdyDatabase): ReminderDao = db.reminderDao()

    @Provides
    fun provideMaintenanceLogDao(db: VerdyDatabase): MaintenanceLogDao = db.maintenanceLogDao()
}
