package com.verdy.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.verdy.MainActivity
import com.verdy.R
import com.verdy.domain.model.enums.MaintenanceAction
import com.verdy.domain.model.enums.ReminderType

object NotificationHelper {

    const val CHANNEL_ID = "verdy_reminders"
    private const val CHANNEL_NAME = "Recordatorios de plantas"
    private const val CHANNEL_DESC = "Notificaciones de cuidado de tus plantas"

    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_PLANT_ID = "extra_plant_id"
    const val EXTRA_REMINDER_TYPE = "extra_reminder_type"
    const val EXTRA_ACTION = "extra_action"

    const val ACTION_DONE = "com.verdy.action.DONE"
    const val ACTION_POSTPONE = "com.verdy.action.POSTPONE"

    fun createChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = CHANNEL_DESC
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun buildNotification(
        context: Context,
        notificationId: Int,
        plantName: String,
        reminderType: ReminderType,
        reminderId: Long,
        plantId: Long,
        customLabel: String? = null
    ): android.app.Notification {
        val title = buildTitle(context, reminderType, plantName, customLabel)
        val body = buildBody(context, reminderType, plantName, customLabel)

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_plant", plantId)
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context, notificationId, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val donePendingIntent = buildActionPendingIntent(
            context, notificationId + 1000, reminderId, plantId, reminderType, ACTION_DONE
        )
        val postponePendingIntent = buildActionPendingIntent(
            context, notificationId + 2000, reminderId, plantId, reminderType, ACTION_POSTPONE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(0, context.getString(R.string.action_done), donePendingIntent)
            .addAction(0, context.getString(R.string.action_postpone), postponePendingIntent)
            .build()
    }

    private fun buildTitle(
        context: Context,
        type: ReminderType,
        plantName: String,
        customLabel: String?
    ): String = when (type) {
        ReminderType.WATERING -> context.getString(R.string.notification_watering_title, plantName)
        ReminderType.FERTILIZING -> context.getString(R.string.notification_fertilizing_title, plantName)
        else -> context.getString(R.string.notification_reminder_title, customLabel ?: type.name)
    }

    private fun buildBody(
        context: Context,
        type: ReminderType,
        plantName: String,
        customLabel: String?
    ): String = when (type) {
        ReminderType.WATERING -> context.getString(R.string.notification_watering_body, plantName)
        ReminderType.FERTILIZING -> context.getString(R.string.notification_fertilizing_body, plantName)
        else -> context.getString(
            R.string.notification_reminder_body,
            plantName,
            customLabel ?: type.name
        )
    }

    private fun buildActionPendingIntent(
        context: Context,
        requestCode: Int,
        reminderId: Long,
        plantId: Long,
        type: ReminderType,
        action: String
    ): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_PLANT_ID, plantId)
            putExtra(EXTRA_REMINDER_TYPE, type.name)
            putExtra(EXTRA_ACTION, if (action == ACTION_DONE) MaintenanceAction.DONE.name else MaintenanceAction.POSTPONED.name)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
