package com.verdy.domain.model

import com.verdy.domain.model.enums.IdentificationConfidence
import com.verdy.domain.model.enums.SunExposure

data class PlantIdentificationResult(
    val commonName: String,
    val scientificName: String,
    val sunExposure: SunExposure,
    val wateringFrequencyDays: Int,
    val curiosities: String,
    val regions: String,
    val confidence: IdentificationConfidence = IdentificationConfidence.MEDIUM,
    val alternativeNames: List<String> = emptyList(),
    val fertilizingFrequencyDays: Int? = null,
    val fertilizerType: String? = null,
    val hasFlowers: Boolean? = null,
    val careSummary: String = "",
    val suggestions: String = ""
)
