package com.verdy.data.worker

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.verdy.domain.model.MaintenanceLog
import com.verdy.domain.model.enums.MaintenanceAction
import com.verdy.domain.model.enums.ReminderType
import com.verdy.domain.repository.MaintenanceRepository
import com.verdy.domain.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var maintenanceRepository: MaintenanceRepository

    @Inject
    lateinit var reminderRepository: ReminderRepository

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1)
        val plantId = intent.getLongExtra(NotificationHelper.EXTRA_PLANT_ID, -1)
        val typeStr = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_TYPE) ?: return
        val actionStr = intent.getStringExtra(NotificationHelper.EXTRA_ACTION) ?: return

        if (reminderId == -1L || plantId == -1L) return

        val type = runCatching { ReminderType.valueOf(typeStr) }.getOrNull() ?: return
        val action = runCatching { MaintenanceAction.valueOf(actionStr) }.getOrNull() ?: return

        // Cancel the notification
        val notificationId = (reminderId % Int.MAX_VALUE).toInt()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)

        // Log the maintenance action and update reminder startDate if DONE
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val today = LocalDate.now()
                val log = MaintenanceLog(
                    plantId = plantId,
                    date = if (action == MaintenanceAction.POSTPONED) today.plusDays(1) else today,
                    type = type,
                    action = action
                )
                maintenanceRepository.addMaintenanceLog(log)

                if (action == MaintenanceAction.DONE) {
                    val reminder = reminderRepository.getReminderById(reminderId)
                    reminder?.let {
                        reminderRepository.updateReminder(it.copy(startDate = today))
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
