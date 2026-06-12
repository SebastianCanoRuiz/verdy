package com.verdy.data.repository

import com.verdy.data.local.db.dao.PlantDao
import com.verdy.data.local.db.entity.PlantEntity
import com.verdy.domain.model.Plant
import com.verdy.domain.repository.PlantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlantRepositoryImpl @Inject constructor(
    private val plantDao: PlantDao
) : PlantRepository {

    override fun getAllPlants(): Flow<List<Plant>> =
        plantDao.getAllPlants().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getPlantById(id: Long): Plant? =
        plantDao.getPlantById(id)?.toDomain()

    override suspend fun addPlant(plant: Plant): Long =
        plantDao.insertPlant(PlantEntity.fromDomain(plant))

    override suspend fun updatePlant(plant: Plant) =
        plantDao.updatePlant(PlantEntity.fromDomain(plant))

    override suspend fun deletePlant(id: Long) =
        plantDao.deletePlant(id)

    override suspend fun getPlantCount(): Int =
        plantDao.getPlantCount()
}
