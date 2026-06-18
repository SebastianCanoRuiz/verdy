package com.verdy.domain.model

import com.verdy.domain.model.enums.IdentificationConfidence
import com.verdy.domain.model.enums.PlantStatus

data class PlantHealthEvaluationResult(
    val suggestedStatus: PlantStatus,
    val confidence: IdentificationConfidence = IdentificationConfidence.MEDIUM,
    val observations: String = "",
    val possibleIssues: List<String> = emptyList(),
    val recommendedActions: List<String> = emptyList(),
    val photoTips: String = "",
    val summary: String = ""
)
