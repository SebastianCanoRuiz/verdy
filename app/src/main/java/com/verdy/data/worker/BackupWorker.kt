package com.verdy.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.verdy.data.local.datastore.AppPreferences
import com.verdy.data.transfer.GardenFileManager
import com.verdy.domain.usecase.transfer.ExportGardenUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val exportGardenUseCase: ExportGardenUseCase,
    private val gardenFileManager: GardenFileManager,
    private val appPreferences: AppPreferences
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return exportGardenUseCase().fold(
            onSuccess = { data ->
                gardenFileManager.exportToInternalBackup(data).fold(
                    onSuccess = {
                        appPreferences.setLastBackupDate(LocalDate.now().toString())
                        Result.success()
                    },
                    onFailure = { Result.retry() }
                )
            },
            onFailure = { Result.retry() }
        )
    }
}
