package com.verdy.domain.usecase.ai

import com.verdy.data.remote.OpenAiClient
import com.verdy.domain.model.PlantIdentificationResult
import javax.inject.Inject

class IdentifyPlantUseCase @Inject constructor(
    private val openAiClient: OpenAiClient
) {
    suspend operator fun invoke(imagePath: String): Result<PlantIdentificationResult> =
        openAiClient.identifyPlant(imagePath)
}
