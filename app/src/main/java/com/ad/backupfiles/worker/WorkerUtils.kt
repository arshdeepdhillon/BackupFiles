package com.ad.backupfiles.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ad.backupfiles.R
import com.ad.backupfiles.receiver.UploadCancelReceiver

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

// START: Worker related data
const val MAIN_CHANNEL_ID = "MAIN_BACKUP_NOTIFICATION"
const val MAIN_CHANNEL_NAME = "Main Backup Information"
const val MAIN_CHANNEL_DESC = "Main Backup Information"
const val DETAILED_CHANNEL_ID = "DETAIL_BACKUP_NOTIFICATION"
const val DETAILED_CHANNEL_NAME = "Detailed Backup Information"
const val DETAILED_CHANNEL_DESC = "Displays Detailed Live Backup Information"

const val NOTIFICATION_ID = 1
val BACKUP_NOTIFICATION_TITLE: CharSequence = "Backing Up Data"
val SYNC_NOTIFICATION_TITLE: CharSequence = "Syncing Data"
// END: Worker related data

// START: Broadcast receiver related data
const val CANCEL_ACTION_KEY = "cancel_worker_tag"
const val CANCEL_ACTION_NAME = "cancel_upload"
// END: Broadcast receiver related data

// Use 0 to represent the absence of an icon
const val NULL_ICON = 0

/**
 * Creates a PendingIntent for the cancellation action in a notification.
 *
 * This function generates a PendingIntent that can be used to handle cancellation actions
 * associated with a notification. It is typically used in conjunction with a notification to
 * allow the user to cancel or dismiss the notification.
 *
 * @param ctx The context used for retrieving system services and resources.
 * @param pendingIntentKey A unique key value associated with the PendingIntent.
 *                              It helps differentiate multiple pending intents.
 *
 * @return A PendingIntent for the cancellation action in a notification.
 */
private fun createCancelIntent(ctx: Context, pendingIntentKey: WorkerType): PendingIntent {
    val cancelUploadIntent = Intent(ctx, UploadCancelReceiver::class.java).apply {
        action = CANCEL_ACTION_NAME
        putExtra(CANCEL_ACTION_KEY, pendingIntentKey.name)
    }

    // Unique requestCode is required for reflecting the current worker tag.
    return PendingIntent.getBroadcast(
        ctx,
        if (pendingIntentKey == WorkerType.BACKUP) 0 else 1,
        cancelUploadIntent,
        PendingIntent.FLAG_IMMUTABLE,
    )
}

/**
 * Creates a notification with the given message and optional parameters.
 *
 * @param message The message to display in the notification.
 * @param ctx The context used to create the notification.
 * @param pendingIntentKey A key associated with a pending intent (optional).
 * @param notificationTitle The title for the notification (default is [BACKUP_NOTIFICATION_TITLE]).
 */
fun makeNotification(
    message: String,
    ctx: Context,
    pendingIntentKey: WorkerType? = null,
    notificationTitle: CharSequence = BACKUP_NOTIFICATION_TITLE,
) {
    // Check notification is enabled before creating it.
    if (NotificationManagerCompat.from(ctx).areNotificationsEnabled()) {
        val builder = NotificationCompat.Builder(ctx, MAIN_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(notificationTitle)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))

        pendingIntentKey?.let { key -> builder.addAction(NULL_ICON, ctx.getString(R.string.cancel_notification), createCancelIntent(ctx, key)) }

        // Show the notification
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, builder.build())
        }
    }
}

/**
 * Silently updates the main Notification channel.
 *
 * @param message The new message to be displayed in the notification.
 * @param ctx The context used for retrieving system services and resources.
 * @param pendingIntentKey An optional key value for a PendingIntent if needed.
 * @param notificationTitle The title to be displayed in the notification. Defaults to [BACKUP_NOTIFICATION_TITLE].
 */
fun updateNotificationMessage(
    message: String,
    ctx: Context,
    pendingIntentKey: WorkerType? = null,
    notificationTitle: CharSequence = BACKUP_NOTIFICATION_TITLE,
) {
    // Check notification is enabled before creating it.
    if (NotificationManagerCompat.from(ctx).areNotificationsEnabled()) {
        val builder = NotificationCompat.Builder(ctx, DETAILED_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(notificationTitle)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVibrate(LongArray(0))

        pendingIntentKey?.let { key -> builder.addAction(NULL_ICON, ctx.getString(R.string.cancel_notification), createCancelIntent(ctx, key)) }
        // Show the notification
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, builder.build())
        }
    }
}
