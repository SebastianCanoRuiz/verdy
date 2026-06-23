package com.verdy.domain.usecase.ai

import com.verdy.data.remote.OpenAiClient
import javax.inject.Inject

class GetPlantCuriositiesUseCase @Inject constructor(
    private val openAiClient: OpenAiClient
) {
    suspend operator fun invoke(plantName: String): Result<String> =
        openAiClient.getCuriosities(plantName)
}
