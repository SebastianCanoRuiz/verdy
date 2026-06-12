package com.verdy.domain.usecase.plant

import com.verdy.domain.model.Plant
import com.verdy.domain.repository.PlantRepository
import javax.inject.Inject

class AddPlantUseCase @Inject constructor(
    private val plantRepository: PlantRepository
) {
    suspend operator fun invoke(plant: Plant): Result<Long> = runCatching {
        require(plant.customName.isNotBlank()) { "El nombre personalizado no puede estar vacío" }
        require(plant.commonName.isNotBlank()) { "El nombre común no puede estar vacío" }
        require(plant.careInfo.wateringFrequencyDays > 0) { "La frecuencia de riego debe ser mayor a 0" }
        plantRepository.addPlant(plant)
    }
}
