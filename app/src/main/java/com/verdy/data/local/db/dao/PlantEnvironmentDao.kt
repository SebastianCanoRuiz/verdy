package com.verdy.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.verdy.data.local.db.entity.PlantEnvironmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantEnvironmentDao {

    @Query("SELECT * FROM plant_environments ORDER BY sort_order ASC, name ASC")
    fun getAll(): Flow<List<PlantEnvironmentEntity>>

    @Query("SELECT * FROM plant_environments WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PlantEnvironmentEntity?

    @Query("SELECT * FROM plant_environments WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name)) LIMIT 1")
    suspend fun getByName(name: String): PlantEnvironmentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(environment: PlantEnvironmentEntity): Long

    @Update
    suspend fun update(environment: PlantEnvironmentEntity)

    @Query("DELETE FROM plant_environments WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM plants WHERE environment_id = :environmentId")
    suspend fun countPlantsInEnvironment(environmentId: Long): Int

    @Query("UPDATE plants SET environment_id = NULL WHERE environment_id = :environmentId")
    suspend fun clearEnvironmentFromPlants(environmentId: Long)

    @Query("SELECT MAX(sort_order) FROM plant_environments")
    suspend fun getMaxSortOrder(): Int?
}
