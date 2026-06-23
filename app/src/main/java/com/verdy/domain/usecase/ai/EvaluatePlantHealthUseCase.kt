package com.verdy.domain.usecase.ai

import com.verdy.data.remote.OpenAiClient
import com.verdy.domain.model.PlantHealthEvaluationResult
import com.verdy.domain.model.enums.PlantStatus
import javax.inject.Inject

class EvaluatePlantHealthUseCase @Inject constructor(
    private val openAiClient: OpenAiClient
) {
    suspend operator fun invoke(
        imagePath: String,
        plantName: String,
        scientificName: String?,
        currentStatus: PlantStatus
    ): Result<PlantHealthEvaluationResult> =
        openAiClient.evaluatePlantHealth(imagePath, plantName, scientificName, currentStatus)
}
