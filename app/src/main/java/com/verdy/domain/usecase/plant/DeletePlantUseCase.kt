package com.verdy.domain.usecase.plant

import com.verdy.domain.repository.MaintenanceRepository
import com.verdy.domain.repository.PlantRepository
import com.verdy.domain.repository.ReminderRepository
import javax.inject.Inject

/** Deletes a plant along with all its associated reminders and maintenance history */
class DeletePlantUseCase @Inject constructor(
    private val plantRepository: PlantRepository,
    private val reminderRepository: ReminderRepository,
    private val maintenanceRepository: MaintenanceRepository
) {
    suspend operator fun invoke(plantId: Long): Result<Unit> = runCatching {
        maintenanceRepository.deleteMaintenanceLogsForPlant(plantId)
        reminderRepository.deleteRemindersByPlantId(plantId)
        plantRepository.deletePlant(plantId)
    }
}
