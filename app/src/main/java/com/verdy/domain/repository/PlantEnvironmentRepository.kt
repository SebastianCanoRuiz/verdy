package com.verdy.domain.repository

import com.verdy.domain.model.PlantEnvironment
import kotlinx.coroutines.flow.Flow

interface PlantEnvironmentRepository {
    fun getAllEnvironments(): Flow<List<PlantEnvironment>>
    suspend fun getEnvironmentById(id: Long): PlantEnvironment?
    suspend fun createEnvironment(name: String): Result<Long>
    suspend fun updateEnvironment(id: Long, name: String): Result<Unit>
    suspend fun deleteEnvironment(id: Long): Result<Unit>
    suspend fun countPlantsInEnvironment(environmentId: Long): Int
}
