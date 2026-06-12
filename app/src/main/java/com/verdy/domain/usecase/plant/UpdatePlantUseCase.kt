package com.verdy.domain.usecase.plant

import com.verdy.domain.model.Plant
import com.verdy.domain.repository.PlantRepository
import javax.inject.Inject

class UpdatePlantUseCase @Inject constructor(
    private val plantRepository: PlantRepository
) {
    suspend operator fun invoke(plant: Plant): Result<Unit> = runCatching {
        require(plant.id > 0) { "ID de planta inválido" }
        require(plant.customName.isNotBlank()) { "El nombre personalizado no puede estar vacío" }
        require(plant.commonName.isNotBlank()) { "El nombre común no puede estar vacío" }
        plantRepository.updatePlant(plant)
    }
}
