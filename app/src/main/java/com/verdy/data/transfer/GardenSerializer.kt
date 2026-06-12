package com.verdy.data.transfer

import com.verdy.domain.model.CareInfo
import com.verdy.domain.model.MaintenanceLog
import com.verdy.domain.model.Plant
import com.verdy.domain.model.Reminder
import com.verdy.domain.model.ReminderFrequency
import com.verdy.domain.model.enums.MaintenanceAction
import com.verdy.domain.model.enums.PlantStatus
import com.verdy.domain.model.enums.ReminderType
import com.verdy.domain.model.enums.SunExposure
import com.verdy.domain.usecase.transfer.GardenExportData
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

private val json = Json { prettyPrint = false; ignoreUnknownKeys = true }

// ── Serializable DTOs ─────────────────────────────────────────────────────────

@Serializable
data class PlantDto(
    val id: Long,
    val customName: String,
    val commonName: String,
    val scientificName: String? = null,
    val photoFileName: String? = null,
    val acquisitionDate: Long? = null,
    val location: String? = null,
    val notes: String? = null,
    val status: String,
    val wateringFrequencyDays: Int,
    val fertilizingFrequencyDays: Int? = null,
    val fertilizerType: String? = null,
    val waterAmountMl: Int? = null,
    val sunExposure: String
)

@Serializable
data class ReminderDto(
    val id: Long,
    val plantId: Long,
    val type: String,
    val customLabel: String? = null,
    val startDate: Long,
    val frequencyType: String,
    val frequencyValue: Int? = null,
    val isActive: Boolean
)

@Serializable
data class MaintenanceLogDto(
    val id: Long,
    val plantId: Long,
    val date: Long,
    val type: String,
    val action: String,
    val notes: String? = null
)

@Serializable
data class GardenDto(
    val version: Int = 1,
    val plants: List<PlantDto>,
    val reminders: List<ReminderDto>,
    val maintenanceLogs: List<MaintenanceLogDto>
)

// ── Conversion functions ──────────────────────────────────────────────────────

fun GardenExportData.toDto(photoFileNames: Map<Long, String> = emptyMap()): GardenDto {
    return GardenDto(
        plants = plants.map { plant ->
            PlantDto(
                id = plant.id,
                customName = plant.customName,
                commonName = plant.commonName,
                scientificName = plant.scientificName,
                photoFileName = photoFileNames[plant.id],
                acquisitionDate = plant.acquisitionDate?.toEpochDay(),
                location = plant.location,
                notes = plant.notes,
                status = plant.status.name,
                wateringFrequencyDays = plant.careInfo.wateringFrequencyDays,
                fertilizingFrequencyDays = plant.careInfo.fertilizingFrequencyDays,
                fertilizerType = plant.careInfo.fertilizerType,
                waterAmountMl = plant.careInfo.waterAmountMl,
                sunExposure = plant.careInfo.sunExposure.name
            )
        },
        reminders = reminders.map { r ->
            val (freqType, freqVal) = frequencyToStrings(r.frequency)
            ReminderDto(
                id = r.id,
                plantId = r.plantId,
                type = r.type.name,
                customLabel = r.customLabel,
                startDate = r.startDate.toEpochDay(),
                frequencyType = freqType,
                frequencyValue = freqVal,
                isActive = r.isActive
            )
        },
        maintenanceLogs = maintenanceLogs.map { log ->
            MaintenanceLogDto(
                id = log.id,
                plantId = log.plantId,
                date = log.date.toEpochDay(),
                type = log.type.name,
                action = log.action.name,
                notes = log.notes
            )
        }
    )
}

fun GardenDto.toDomain(): GardenExportData {
    return GardenExportData(
        plants = plants.map { dto ->
            Plant(
                id = dto.id,
                customName = dto.customName,
                commonName = dto.commonName,
                scientificName = dto.scientificName,
                photoUri = null, // photo URIs are local, can't restore
                acquisitionDate = dto.acquisitionDate?.let { LocalDate.ofEpochDay(it) },
                location = dto.location,
                notes = dto.notes,
                status = runCatching { PlantStatus.valueOf(dto.status) }.getOrDefault(PlantStatus.HEALTHY),
                careInfo = CareInfo(
                    wateringFrequencyDays = dto.wateringFrequencyDays,
                    fertilizingFrequencyDays = dto.fertilizingFrequencyDays,
                    fertilizerType = dto.fertilizerType,
                    waterAmountMl = dto.waterAmountMl,
                    sunExposure = runCatching { SunExposure.valueOf(dto.sunExposure) }.getOrDefault(SunExposure.SEMI_SHADE)
                )
            )
        },
        reminders = reminders.map { dto ->
            Reminder(
                id = dto.id,
                plantId = dto.plantId,
                type = runCatching { ReminderType.valueOf(dto.type) }.getOrDefault(ReminderType.WATERING),
                customLabel = dto.customLabel,
                startDate = LocalDate.ofEpochDay(dto.startDate),
                frequency = stringsToFrequency(dto.frequencyType, dto.frequencyValue),
                isActive = dto.isActive
            )
        },
        maintenanceLogs = maintenanceLogs.map { dto ->
            MaintenanceLog(
                id = dto.id,
                plantId = dto.plantId,
                date = LocalDate.ofEpochDay(dto.date),
                type = runCatching { ReminderType.valueOf(dto.type) }.getOrDefault(ReminderType.WATERING),
                action = runCatching { MaintenanceAction.valueOf(dto.action) }.getOrDefault(MaintenanceAction.DONE),
                notes = dto.notes
            )
        }
    )
}

fun GardenDto.toJsonString(): String = json.encodeToString(this)

fun gardenDtoFromJson(jsonStr: String): GardenDto = json.decodeFromString(jsonStr)

private fun frequencyToStrings(freq: ReminderFrequency): Pair<String, Int?> = when (freq) {
    is ReminderFrequency.Once -> "ONCE" to null
    is ReminderFrequency.Daily -> "DAILY" to null
    is ReminderFrequency.Weekly -> "WEEKLY" to null
    is ReminderFrequency.EveryXDays -> "EVERY_X_DAYS" to freq.days
    is ReminderFrequency.Monthly -> "MONTHLY" to null
}

private fun stringsToFrequency(type: String, value: Int?): ReminderFrequency = when (type) {
    "ONCE" -> ReminderFrequency.Once
    "DAILY" -> ReminderFrequency.Daily
    "WEEKLY" -> ReminderFrequency.Weekly
    "EVERY_X_DAYS" -> ReminderFrequency.EveryXDays(value ?: 7)
    "MONTHLY" -> ReminderFrequency.Monthly
    else -> ReminderFrequency.Weekly
}
