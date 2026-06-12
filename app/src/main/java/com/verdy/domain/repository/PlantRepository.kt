package com.verdy.domain.repository

import com.verdy.domain.model.Plant
import kotlinx.coroutines.flow.Flow

interface PlantRepository {
    fun getAllPlants(): Flow<List<Plant>>
    suspend fun getPlantById(id: Long): Plant?
    suspend fun addPlant(plant: Plant): Long
    suspend fun updatePlant(plant: Plant)
    suspend fun deletePlant(id: Long)
    suspend fun getPlantCount(): Int
}
