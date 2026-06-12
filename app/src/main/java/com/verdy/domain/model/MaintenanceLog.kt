package com.verdy.domain.model

import com.verdy.domain.model.enums.MaintenanceAction
import com.verdy.domain.model.enums.ReminderType
import java.time.LocalDate

/**
 * A single entry in a plant's maintenance history.
 * Created whenever the user marks a reminder as done, postponed, or ignored.
 */
data class MaintenanceLog(
    val id: Long = 0,
    val plantId: Long,
    val date: LocalDate,
    val type: ReminderType,
    val action: MaintenanceAction,
    val notes: String? = null
)
