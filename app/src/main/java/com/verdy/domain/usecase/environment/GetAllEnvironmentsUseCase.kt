package com.verdy.domain.usecase.environment

import com.verdy.domain.model.PlantEnvironment
import com.verdy.domain.repository.PlantEnvironmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllEnvironmentsUseCase @Inject constructor(
    private val repository: PlantEnvironmentRepository
) {
    operator fun invoke(): Flow<List<PlantEnvironment>> = repository.getAllEnvironments()
}
