package com.verdy.domain.usecase.transfer

import com.verdy.domain.repository.MaintenanceRepository
import com.verdy.domain.repository.PlantEnvironmentRepository
import com.verdy.domain.repository.PlantRepository
import com.verdy.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ImportGardenUseCase @Inject constructor(
    private val plantRepository: PlantRepository,
    private val reminderRepository: ReminderRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val environmentRepository: PlantEnvironmentRepository
) {
    suspend operator fun invoke(
        data: GardenExportData,
        replaceExisting: Boolean = false
    ): Result<Unit> = runCatching {
        if (replaceExisting) {
            // Deletion handled by merge strategy; plants are always added
        }

        val environmentIdMap = mutableMapOf<Long, Long>()
        val existingEnvironments = environmentRepository.getAllEnvironments().first()
        for (environment in data.environments) {
            val existing = existingEnvironments.find {
                it.name.equals(environment.name, ignoreCase = true)
            }
            val newId = existing?.id
                ?: environmentRepository.createEnvironment(environment.name).getOrThrow()
            environmentIdMap[environment.id] = newId
        }

        val plantIdMap = mutableMapOf<Long, Long>()

        for (plant in data.plants) {
            val mappedEnvironmentId = plant.environmentId?.let { environmentIdMap[it] }
            val newId = plantRepository.addPlant(
                plant.copy(id = 0, environmentId = mappedEnvironmentId)
            )
            plantIdMap[plant.id] = newId
        }

        for (reminder in data.reminders) {
            val newPlantId = plantIdMap[reminder.plantId] ?: continue
            reminderRepository.addReminder(reminder.copy(id = 0, plantId = newPlantId))
        }

        for (log in data.maintenanceLogs) {
            val newPlantId = plantIdMap[log.plantId] ?: continue
            maintenanceRepository.addMaintenanceLog(log.copy(id = 0, plantId = newPlantId))
        }
    }
}
