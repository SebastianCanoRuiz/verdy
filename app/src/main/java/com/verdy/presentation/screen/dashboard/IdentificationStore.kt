package com.verdy.presentation.screen.dashboard

import com.verdy.domain.model.PlantIdentificationResult

/**
 * Temporary in-memory store to pass identification results from Dashboard to PlantFormScreen.
 * Cleared immediately after consumption.
 */
object IdentificationStore {
    var pending: PlantIdentificationResult? = null

    fun consume(): PlantIdentificationResult? {
        val result = pending
        pending = null
        return result
    }
}
