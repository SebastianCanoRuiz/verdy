package com.verdy.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.verdy.domain.model.PlantEnvironment

@Entity(
    tableName = "plant_environments",
    indices = [Index(value = ["name"], unique = true)]
)
data class PlantEnvironmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
) {
    fun toDomain(): PlantEnvironment = PlantEnvironment(
        id = id,
        name = name,
        sortOrder = sortOrder
    )

    companion object {
        fun fromDomain(environment: PlantEnvironment): PlantEnvironmentEntity =
            PlantEnvironmentEntity(
                id = environment.id,
                name = environment.name,
                sortOrder = environment.sortOrder
            )
    }
}
