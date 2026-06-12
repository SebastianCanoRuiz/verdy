package com.verdy.data.worker

import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.verdy.domain.model.Reminder
import com.verdy.domain.usecase.reminder.ScheduleReminderUseCase
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Bridges domain scheduling logic with WorkManager scheduling */
@Singleton
class WorkManagerScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val scheduleReminderUseCase: ScheduleReminderUseCase
) {
    fun schedule(reminder: Reminder) {
        val params = scheduleReminderUseCase(reminder)
        val inputData = Data.Builder()
            .putLong(ReminderWorker.KEY_REMINDER_ID, reminder.id)
            .putLong(ReminderWorker.KEY_PLANT_ID, reminder.plantId)
            .putString(ReminderWorker.KEY_REMINDER_TYPE, reminder.type.name)
            .build()

        if (params.isPeriodic && params.periodMillis >= TimeUnit.MINUTES.toMillis(15)) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(
                params.periodMillis, TimeUnit.MILLISECONDS
            )
                .setInitialDelay(params.delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            workManager.enqueueUniquePeriodicWork(
                params.reminderWorkName,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        } else {
            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(params.delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            workManager.enqueueUniqueWork(
                params.reminderWorkName,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    fun cancel(reminder: Reminder) {
        workManager.cancelUniqueWork("reminder_${reminder.id}")
    }
}
