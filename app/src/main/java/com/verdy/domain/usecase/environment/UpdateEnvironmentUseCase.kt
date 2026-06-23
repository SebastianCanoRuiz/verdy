package com.verdy.domain.usecase.environment

import com.verdy.domain.repository.PlantEnvironmentRepository
import javax.inject.Inject

class UpdateEnvironmentUseCase @Inject constructor(
    private val repository: PlantEnvironmentRepository
) {
    suspend operator fun invoke(id: Long, name: String): Result<Unit> =
        repository.updateEnvironment(id, name)
}
