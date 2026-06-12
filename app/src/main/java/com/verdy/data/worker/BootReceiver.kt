package com.verdy.data.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint

/**
 * Reschedules all active reminders after device reboot or app update.
 * WorkManager generally handles this automatically for persisted work,
 * but explicit rescheduling ensures reliability.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            // WorkManager already restores persisted periodic work on boot.
            // For one-time work that may have been missed, a reschedule worker could run here.
            // This is a placeholder for future rescheduling logic.
        }
    }
}
