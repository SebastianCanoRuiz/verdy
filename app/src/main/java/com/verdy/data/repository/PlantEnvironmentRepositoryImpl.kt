package com.verdy.data.repository

import com.verdy.data.local.db.dao.PlantEnvironmentDao
import com.verdy.data.local.db.entity.PlantEnvironmentEntity
import com.verdy.domain.model.PlantEnvironment
import com.verdy.domain.repository.PlantEnvironmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlantEnvironmentRepositoryImpl @Inject constructor(
    private val environmentDao: PlantEnvironmentDao
) : PlantEnvironmentRepository {

    override fun getAllEnvironments(): Flow<List<PlantEnvironment>> =
        environmentDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getEnvironmentById(id: Long): PlantEnvironment? =
        environmentDao.getById(id)?.toDomain()

    override suspend fun createEnvironment(name: String): Result<Long> = runCatching {
        val trimmed = name.trim()
        require(trimmed.isNotBlank()) { "El nombre no puede estar vacío" }
        if (environmentDao.getByName(trimmed) != null) {
            error("Ya existe un ambiente con ese nombre")
        }
        val sortOrder = (environmentDao.getMaxSortOrder() ?: -1) + 1
        environmentDao.insert(
            PlantEnvironmentEntity(name = trimmed, sortOrder = sortOrder)
        )
    }

    override suspend fun updateEnvironment(id: Long, name: String): Result<Unit> = runCatching {
        val trimmed = name.trim()
        require(trimmed.isNotBlank()) { "El nombre no puede estar vacío" }
        val existing = environmentDao.getById(id) ?: error("Ambiente no encontrado")
        val duplicate = environmentDao.getByName(trimmed)
        if (duplicate != null && duplicate.id != id) {
            error("Ya existe un ambiente con ese nombre")
        }
        environmentDao.update(existing.copy(name = trimmed))
    }

    override suspend fun deleteEnvironment(id: Long): Result<Unit> = runCatching {
        environmentDao.getById(id) ?: error("Ambiente no encontrado")
        environmentDao.clearEnvironmentFromPlants(id)
        environmentDao.delete(id)
    }

    override suspend fun countPlantsInEnvironment(environmentId: Long): Int =
        environmentDao.countPlantsInEnvironment(environmentId)
}
