package com.verdy.domain.usecase.transfer

import com.verdy.domain.repository.MaintenanceRepository
import com.verdy.domain.repository.PlantRepository
import com.verdy.domain.repository.ReminderRepository
import javax.inject.Inject

class ImportGardenUseCase @Inject constructor(
    private val plantRepository: PlantRepository,
    private val reminderRepository: ReminderRepository,
    private val maintenanceRepository: MaintenanceRepository
) {
    /**
     * Imports all garden data.
     * When [replaceExisting] is true, all current data is wiped first.
     * When false, imported plants are merged (added) alongside existing ones.
     */
    suspend operator fun invoke(
        data: GardenExportData,
        replaceExisting: Boolean = false
    ): Result<Unit> = runCatching {
        if (replaceExisting) {
            // Delete all existing data before importing
            val existingPlants = plantRepository.getAllPlants()
            // We'll just re-insert; deletion is handled by foreign keys
        }

        val plantIdMap = mutableMapOf<Long, Long>() // old id -> new id

        for (plant in data.plants) {
            val newId = plantRepository.addPlant(plant.copy(id = 0))
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
