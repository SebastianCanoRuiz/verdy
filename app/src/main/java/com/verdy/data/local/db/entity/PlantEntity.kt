package com.verdy.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.verdy.domain.model.CareInfo
import com.verdy.domain.model.Plant
import com.verdy.domain.model.enums.PlantStatus
import com.verdy.domain.model.enums.SunExposure
import java.time.LocalDate

@Entity(tableName = "plants")
data class PlantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "custom_name") val customName: String,
    @ColumnInfo(name = "common_name") val commonName: String,
    @ColumnInfo(name = "scientific_name") val scientificName: String?,
    @ColumnInfo(name = "photo_uri") val photoUri: String?,
    @ColumnInfo(name = "acquisition_date") val acquisitionDate: LocalDate?,
    @ColumnInfo(name = "location") val location: String?,
    @ColumnInfo(name = "notes") val notes: String?,
    @ColumnInfo(name = "status") val status: PlantStatus,

    // CareInfo — embedded directly
    @ColumnInfo(name = "watering_frequency_days") val wateringFrequencyDays: Int,
    @ColumnInfo(name = "fertilizing_frequency_days") val fertilizingFrequencyDays: Int?,
    @ColumnInfo(name = "fertilizer_type") val fertilizerType: String?,
    @ColumnInfo(name = "water_amount_ml") val waterAmountMl: Int?,
    @ColumnInfo(name = "sun_exposure") val sunExposure: SunExposure
) {
    fun toDomain(): Plant = Plant(
        id = id,
        customName = customName,
        commonName = commonName,
        scientificName = scientificName,
        photoUri = photoUri,
        acquisitionDate = acquisitionDate,
        location = location,
        notes = notes,
        status = status,
        careInfo = CareInfo(
            wateringFrequencyDays = wateringFrequencyDays,
            fertilizingFrequencyDays = fertilizingFrequencyDays,
            fertilizerType = fertilizerType,
            waterAmountMl = waterAmountMl,
            sunExposure = sunExposure
        )
    )

    companion object {
        fun fromDomain(plant: Plant): PlantEntity = PlantEntity(
            id = plant.id,
            customName = plant.customName,
            commonName = plant.commonName,
            scientificName = plant.scientificName,
            photoUri = plant.photoUri,
            acquisitionDate = plant.acquisitionDate,
            location = plant.location,
            notes = plant.notes,
            status = plant.status,
            wateringFrequencyDays = plant.careInfo.wateringFrequencyDays,
            fertilizingFrequencyDays = plant.careInfo.fertilizingFrequencyDays,
            fertilizerType = plant.careInfo.fertilizerType,
            waterAmountMl = plant.careInfo.waterAmountMl,
            sunExposure = plant.careInfo.sunExposure
        )
    }
}
