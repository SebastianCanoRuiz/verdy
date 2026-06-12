package com.verdy.domain.usecase.plant

import com.verdy.domain.model.Plant
import com.verdy.domain.repository.PlantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPlantsUseCase @Inject constructor(
    private val plantRepository: PlantRepository
) {
    operator fun invoke(): Flow<List<Plant>> = plantRepository.getAllPlants()
}
