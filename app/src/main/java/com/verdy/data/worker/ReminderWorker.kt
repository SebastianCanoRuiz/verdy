package com.verdy.data.worker

import android.content.Context
import android.app.NotificationManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.verdy.domain.repository.PlantRepository
import com.verdy.domain.repository.ReminderRepository
import com.verdy.domain.model.enums.ReminderType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val plantRepository: PlantRepository,
    private val reminderRepository: ReminderRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val reminderId = inputData.getLong(KEY_REMINDER_ID, -1)
        val plantId = inputData.getLong(KEY_PLANT_ID, -1)
        val typeStr = inputData.getString(KEY_REMINDER_TYPE) ?: return Result.failure()

        if (reminderId == -1L || plantId == -1L) return Result.failure()

        val reminder = reminderRepository.getReminderById(reminderId) ?: return Result.success()
        if (!reminder.isActive) return Result.success()

        val plant = plantRepository.getPlantById(plantId) ?: return Result.success()
        val type = runCatching { ReminderType.valueOf(typeStr) }.getOrNull() ?: return Result.failure()

        NotificationHelper.createChannel(applicationContext)

        val notificationId = (reminderId % Int.MAX_VALUE).toInt()
        val notification = NotificationHelper.buildNotification(
            context = applicationContext,
            notificationId = notificationId,
            plantName = plant.customName,
            reminderType = type,
            reminderId = reminderId,
            plantId = plantId,
            customLabel = reminder.customLabel
        )

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)

        return Result.success()
    }

    companion object {
        const val KEY_REMINDER_ID = "reminder_id"
        const val KEY_PLANT_ID = "plant_id"
        const val KEY_REMINDER_TYPE = "reminder_type"
    }
}
