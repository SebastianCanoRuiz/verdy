package com.verdy.domain.usecase.environment

import com.verdy.domain.repository.PlantEnvironmentRepository
import javax.inject.Inject

class CreateEnvironmentUseCase @Inject constructor(
    private val repository: PlantEnvironmentRepository
) {
    suspend operator fun invoke(name: String): Result<Long> =
        repository.createEnvironment(name)
}
