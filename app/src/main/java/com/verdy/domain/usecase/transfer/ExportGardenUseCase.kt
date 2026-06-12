package com.verdy.domain.usecase.transfer

import com.verdy.domain.model.MaintenanceLog
import com.verdy.domain.model.Plant
import com.verdy.domain.model.Reminder
import com.verdy.domain.repository.MaintenanceRepository
import com.verdy.domain.repository.PlantRepository
import com.verdy.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class GardenExportData(
    val plants: List<Plant>,
    val reminders: List<Reminder>,
    val maintenanceLogs: List<MaintenanceLog>
)

class ExportGardenUseCase @Inject constructor(
    private val plantRepository: PlantRepository,
    private val reminderRepository: ReminderRepository,
    private val maintenanceRepository: MaintenanceRepository
) {
    suspend operator fun invoke(): Result<GardenExportData> = runCatching {
        val plants = plantRepository.getAllPlants().first()
        val reminders = reminderRepository.getAllReminders().first()
        val logs = plants.flatMap { plant ->
            maintenanceRepository.getMaintenanceHistoryForPlant(plant.id).first()
        }
        GardenExportData(plants, reminders, logs)
    }
}
