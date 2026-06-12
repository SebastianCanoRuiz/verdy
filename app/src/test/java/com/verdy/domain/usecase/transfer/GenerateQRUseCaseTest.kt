package com.verdy.domain.usecase.transfer

import com.verdy.domain.model.CareInfo
import com.verdy.domain.model.Plant
import com.verdy.domain.model.enums.PlantStatus
import com.verdy.domain.model.enums.SunExposure
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateQRUseCaseTest {

    private lateinit var useCase: GenerateQRUseCase

    @Before
    fun setUp() {
        useCase = GenerateQRUseCase()
    }

    private fun makePlant(name: String) = Plant(
        id = 1L,
        customName = name,
        commonName = "Common",
        status = PlantStatus.HEALTHY,
        careInfo = CareInfo(wateringFrequencyDays = 7, sunExposure = SunExposure.MEDIUM)
    )

    @Test
    fun `single plant produces QR success result`() {
        val data = GardenExportData(
            plants = listOf(makePlant("Monstera")),
            reminders = emptyList(),
            maintenanceLogs = emptyList()
        )

        val result = useCase(data)

        assertTrue(result is QRResult.Success)
    }

    @Test
    fun `empty garden produces QR success result`() {
        val data = GardenExportData(emptyList(), emptyList(), emptyList())

        val result = useCase(data)

        assertTrue(result is QRResult.Success)
        val payload = (result as QRResult.Success).jsonPayload
        assertTrue(payload.contains("\"plants\":[]"))
    }

    @Test
    fun `many plants with long names triggers TooLarge`() {
        val manyPlants = (1..100).map { i ->
            makePlant("Una planta con nombre muy largo número $i que ocupa mucho espacio en el JSON")
        }
        val data = GardenExportData(manyPlants, emptyList(), emptyList())

        val result = useCase(data)

        assertTrue(result is QRResult.TooLarge)
    }

    @Test
    fun `payload does not exceed QR limit for small garden`() {
        val data = GardenExportData(
            plants = listOf(makePlant("Cactus"), makePlant("Pothos")),
            reminders = emptyList(),
            maintenanceLogs = emptyList()
        )

        val result = useCase(data) as QRResult.Success

        assertTrue(result.jsonPayload.toByteArray().size <= GenerateQRUseCase.MAX_QR_BYTES)
    }
}
