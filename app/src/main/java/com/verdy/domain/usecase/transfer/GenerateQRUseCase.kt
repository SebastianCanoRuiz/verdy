package com.verdy.domain.usecase.transfer

import javax.inject.Inject

sealed class QRResult {
    data class Success(val jsonPayload: String) : QRResult()
    object TooLarge : QRResult()
}

/**
 * Serializes a [GardenExportData] subset (no photos) to a compact JSON string
 * suitable for QR encoding.
 * Maximum safe QR payload is ~2KB for error correction level M.
 */
class GenerateQRUseCase @Inject constructor() {
    /** Returns the JSON string if within size limits, otherwise [QRResult.TooLarge] */
    operator fun invoke(data: GardenExportData): QRResult {
        // Plants serialized to compact representation (no photo URIs — those are local paths)
        val compactPlants = data.plants.map { p ->
            buildString {
                append("{")
                append("\"n\":\"${p.customName.sanitize()}\",")
                append("\"c\":\"${p.commonName.sanitize()}\",")
                p.scientificName?.let { append("\"s\":\"${it.sanitize()}\",") }
                p.location?.let { append("\"l\":\"${it.sanitize()}\",") }
                append("\"st\":\"${p.status.name}\",")
                append("\"wf\":${p.careInfo.wateringFrequencyDays},")
                append("\"se\":\"${p.careInfo.sunExposure.name}\"")
                append("}")
            }
        }

        val json = "{\"v\":1,\"plants\":[${compactPlants.joinToString(",")}]}"

        return if (json.toByteArray(Charsets.UTF_8).size <= MAX_QR_BYTES) {
            QRResult.Success(json)
        } else {
            QRResult.TooLarge
        }
    }

    private fun String.sanitize(): String = replace("\\", "\\\\").replace("\"", "\\\"")

    companion object {
        const val MAX_QR_BYTES = 2048
    }
}
