package com.verdy.domain.model

import com.verdy.domain.model.enums.PlantStatus
import java.time.LocalDate

/**
 * Core domain entity representing a plant in the user's garden.
 */
data class Plant(
    val id: Long = 0,
    val customName: String,
    val commonName: String,
    val scientificName: String? = null,
    val photoUri: String? = null,
    val acquisitionDate: LocalDate? = null,
    val location: String? = null,
    val notes: String? = null,
    val status: PlantStatus = PlantStatus.HEALTHY,
    val careInfo: CareInfo = CareInfo()
)
