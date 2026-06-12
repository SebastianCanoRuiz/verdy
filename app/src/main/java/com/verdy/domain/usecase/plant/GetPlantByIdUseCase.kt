package com.verdy.domain.usecase.plant

import com.verdy.domain.model.Plant
import com.verdy.domain.repository.PlantRepository
import javax.inject.Inject

class GetPlantByIdUseCase @Inject constructor(
    private val plantRepository: PlantRepository
) {
    suspend operator fun invoke(id: Long): Plant? = plantRepository.getPlantById(id)
}
