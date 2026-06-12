package com.verdy.domain.usecase.maintenance

import com.verdy.domain.model.enums.ReminderType
import com.verdy.domain.repository.MaintenanceRepository
import java.time.LocalDate
import javax.inject.Inject

class GetLastCareDateUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository
) {
    suspend operator fun invoke(plantId: Long, type: ReminderType): LocalDate? =
        maintenanceRepository.getLastCareDate(plantId, type)
}
