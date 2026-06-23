package com.verdy.domain.usecase.transfer

import com.verdy.domain.model.MaintenanceLog
import com.verdy.domain.model.Plant
import com.verdy.domain.model.PlantEnvironment
import com.verdy.domain.model.Reminder
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class GardenExportData(
    val plants: List<Plant>,
    val reminders: List<Reminder>,
    val maintenanceLogs: List<MaintenanceLog>,
    val environments: List<PlantEnvironment> = emptyList()
)

class ExportGardenUseCase @Inject constructor(
    private val plantRepository: com.verdy.domain.repository.PlantRepository,
    private val reminderRepository: com.verdy.domain.repository.ReminderRepository,
    private val maintenanceRepository: com.verdy.domain.repository.MaintenanceRepository,
    private val environmentRepository: com.verdy.domain.repository.PlantEnvironmentRepository
) {
    suspend operator fun invoke(): Result<GardenExportData> = runCatching {
        val plants = plantRepository.getAllPlants().first()
        val reminders = reminderRepository.getAllReminders().first()
        val environments = environmentRepository.getAllEnvironments().first()
        val logs = plants.flatMap { plant ->
            maintenanceRepository.getMaintenanceHistoryForPlant(plant.id).first()
        }
        GardenExportData(plants, reminders, logs, environments)
    }
}
