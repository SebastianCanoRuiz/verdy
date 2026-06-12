package com.verdy.domain.usecase.maintenance

import com.verdy.domain.model.MaintenanceLog
import com.verdy.domain.model.enums.MaintenanceAction
import com.verdy.domain.model.enums.ReminderType
import com.verdy.domain.repository.MaintenanceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class RegisterMaintenanceUseCaseTest {

    private lateinit var maintenanceRepository: MaintenanceRepository
    private lateinit var registerMaintenance: RegisterMaintenanceUseCase

    @Before
    fun setUp() {
        maintenanceRepository = mockk()
        registerMaintenance = RegisterMaintenanceUseCase(maintenanceRepository)
    }

    @Test
    fun `invoke with valid params creates log and returns id`() = runTest {
        val logSlot = slot<MaintenanceLog>()
        coEvery { maintenanceRepository.addMaintenanceLog(capture(logSlot)) } returns 42L

        val result = registerMaintenance(
            plantId = 1L,
            type = ReminderType.WATERING,
            action = MaintenanceAction.DONE
        )

        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull())
        assertEquals(1L, logSlot.captured.plantId)
        assertEquals(ReminderType.WATERING, logSlot.captured.type)
        assertEquals(MaintenanceAction.DONE, logSlot.captured.action)
        assertEquals(LocalDate.now(), logSlot.captured.date)
    }

    @Test
    fun `invoke with custom date uses provided date`() = runTest {
        val logSlot = slot<MaintenanceLog>()
        val customDate = LocalDate.of(2025, 6, 15)
        coEvery { maintenanceRepository.addMaintenanceLog(capture(logSlot)) } returns 1L

        registerMaintenance(
            plantId = 1L,
            type = ReminderType.FERTILIZING,
            action = MaintenanceAction.DONE,
            date = customDate
        )

        assertEquals(customDate, logSlot.captured.date)
    }

    @Test
    fun `invoke with invalid plantId returns failure`() = runTest {
        val result = registerMaintenance(
            plantId = -1L,
            type = ReminderType.WATERING,
            action = MaintenanceAction.DONE
        )

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { maintenanceRepository.addMaintenanceLog(any()) }
    }

    @Test
    fun `invoke with notes stores notes in log`() = runTest {
        val logSlot = slot<MaintenanceLog>()
        coEvery { maintenanceRepository.addMaintenanceLog(capture(logSlot)) } returns 1L

        registerMaintenance(
            plantId = 1L,
            type = ReminderType.PRUNING,
            action = MaintenanceAction.DONE,
            notes = "Poda de primavera"
        )

        assertEquals("Poda de primavera", logSlot.captured.notes)
    }

    @Test
    fun `invoke POSTPONED action saves correctly`() = runTest {
        val logSlot = slot<MaintenanceLog>()
        coEvery { maintenanceRepository.addMaintenanceLog(capture(logSlot)) } returns 1L

        registerMaintenance(
            plantId = 2L,
            type = ReminderType.WATERING,
            action = MaintenanceAction.POSTPONED
        )

        assertEquals(MaintenanceAction.POSTPONED, logSlot.captured.action)
    }
}
