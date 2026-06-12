package com.verdy.domain.usecase.maintenance

import com.verdy.domain.model.MaintenanceLog
import com.verdy.domain.model.enums.MaintenanceAction
import com.verdy.domain.model.enums.ReminderType
import com.verdy.domain.repository.MaintenanceRepository
import java.time.LocalDate
import javax.inject.Inject

class RegisterMaintenanceUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository
) {
    suspend operator fun invoke(
        plantId: Long,
        type: ReminderType,
        action: MaintenanceAction,
        notes: String? = null,
        date: LocalDate = LocalDate.now()
    ): Result<Long> = runCatching {
        require(plantId > 0) { "ID de planta inválido" }
        val log = MaintenanceLog(
            plantId = plantId,
            date = date,
            type = type,
            action = action,
            notes = notes
        )
        maintenanceRepository.addMaintenanceLog(log)
    }
}
