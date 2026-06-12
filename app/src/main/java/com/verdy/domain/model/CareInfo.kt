package com.verdy.domain.model

import com.verdy.domain.model.enums.SunExposure

/**
 * Information about how to care for a plant.
 * All frequency values are in days.
 */
data class CareInfo(
    val wateringFrequencyDays: Int = 7,
    val fertilizingFrequencyDays: Int? = null,
    val fertilizerType: String? = null,
    val waterAmountMl: Int? = null,
    val sunExposure: SunExposure = SunExposure.SEMI_SHADE
)
