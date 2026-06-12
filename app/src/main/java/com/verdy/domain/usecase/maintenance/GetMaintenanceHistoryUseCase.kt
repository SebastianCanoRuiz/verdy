package com.verdy.domain.usecase.maintenance

import com.verdy.domain.model.MaintenanceLog
import com.verdy.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMaintenanceHistoryUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository
) {
    operator fun invoke(plantId: Long): Flow<List<MaintenanceLog>> =
        maintenanceRepository.getMaintenanceHistoryForPlant(plantId)
}
