package com.verdy.domain.usecase.transfer

import com.verdy.domain.model.CareInfo
import com.verdy.domain.model.Plant
import com.verdy.domain.model.Reminder
import com.verdy.domain.model.enums.PlantStatus
import com.verdy.domain.model.enums.SunExposure
import org.json.JSONObject
import javax.inject.Inject

/** Decodes a QR payload string back into a partial [GardenExportData] */
class DecodeQRUseCase @Inject constructor() {
    operator fun invoke(payload: String): Result<GardenExportData> = runCatching {
        val root = JSONObject(payload)
        val version = root.optInt("v", 1)
        require(version == 1) { "Versión de QR no soportada: $version" }

        val plantsArray = root.getJSONArray("plants")
        val plants = (0 until plantsArray.length()).map { i ->
            val p = plantsArray.getJSONObject(i)
            Plant(
                id = 0,
                customName = p.getString("n"),
                commonName = p.getString("c"),
                scientificName = p.optString("s").takeIf { it.isNotEmpty() },
                location = p.optString("l").takeIf { it.isNotEmpty() },
                status = runCatching { PlantStatus.valueOf(p.optString("st", "HEALTHY")) }
                    .getOrDefault(PlantStatus.HEALTHY),
                careInfo = CareInfo(
                    wateringFrequencyDays = p.optInt("wf", 7),
                    sunExposure = runCatching { SunExposure.valueOf(p.optString("se", "SEMI_SHADE")) }
                        .getOrDefault(SunExposure.SEMI_SHADE)
                )
            )
        }

        GardenExportData(
            plants = plants,
            reminders = emptyList(),
            maintenanceLogs = emptyList()
        )
    }
}
