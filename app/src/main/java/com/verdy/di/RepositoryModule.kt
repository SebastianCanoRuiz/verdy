package com.verdy.di

import com.verdy.data.repository.MaintenanceRepositoryImpl
import com.verdy.data.repository.PlantRepositoryImpl
import com.verdy.data.repository.ReminderRepositoryImpl
import com.verdy.domain.repository.MaintenanceRepository
import com.verdy.domain.repository.PlantRepository
import com.verdy.domain.repository.ReminderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPlantRepository(impl: PlantRepositoryImpl): PlantRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(impl: MaintenanceRepositoryImpl): MaintenanceRepository
}
