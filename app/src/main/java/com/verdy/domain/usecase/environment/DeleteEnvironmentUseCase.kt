package com.verdy.domain.usecase.environment

import com.verdy.domain.repository.PlantEnvironmentRepository
import javax.inject.Inject

class DeleteEnvironmentUseCase @Inject constructor(
    private val repository: PlantEnvironmentRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> =
        repository.deleteEnvironment(id)
}
