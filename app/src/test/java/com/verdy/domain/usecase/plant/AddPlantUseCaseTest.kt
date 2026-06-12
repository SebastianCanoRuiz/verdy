package com.verdy.domain.usecase.plant

import com.verdy.domain.model.CareInfo
import com.verdy.domain.model.Plant
import com.verdy.domain.model.enums.PlantStatus
import com.verdy.domain.model.enums.SunExposure
import com.verdy.domain.repository.PlantRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddPlantUseCaseTest {

    private lateinit var plantRepository: PlantRepository
    private lateinit var addPlantUseCase: AddPlantUseCase

    private val validPlant = Plant(
        customName = "Mi Monstera",
        commonName = "Monstera deliciosa",
        status = PlantStatus.HEALTHY,
        careInfo = CareInfo(
            wateringFrequencyDays = 7,
            sunExposure = SunExposure.MEDIUM
        )
    )

    @Before
    fun setUp() {
        plantRepository = mockk()
        addPlantUseCase = AddPlantUseCase(plantRepository)
    }

    @Test
    fun `invoke with valid plant returns success with new id`() = runTest {
        coEvery { plantRepository.addPlant(any()) } returns 1L

        val result = addPlantUseCase(validPlant)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        coVerify(exactly = 1) { plantRepository.addPlant(validPlant) }
    }

    @Test
    fun `invoke with blank customName returns failure`() = runTest {
        val plant = validPlant.copy(customName = "  ")

        val result = addPlantUseCase(plant)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("nombre personalizado") == true)
        coVerify(exactly = 0) { plantRepository.addPlant(any()) }
    }

    @Test
    fun `invoke with blank commonName returns failure`() = runTest {
        val plant = validPlant.copy(commonName = "")

        val result = addPlantUseCase(plant)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { plantRepository.addPlant(any()) }
    }

    @Test
    fun `invoke with zero wateringFrequency returns failure`() = runTest {
        val plant = validPlant.copy(careInfo = validPlant.careInfo.copy(wateringFrequencyDays = 0))

        val result = addPlantUseCase(plant)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { plantRepository.addPlant(any()) }
    }

    @Test
    fun `invoke propagates repository exception as failure`() = runTest {
        coEvery { plantRepository.addPlant(any()) } throws RuntimeException("DB error")

        val result = addPlantUseCase(validPlant)

        assertTrue(result.isFailure)
        assertEquals("DB error", result.exceptionOrNull()?.message)
    }
}
