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

// Worker Tag info
const val WORKER_TAG = "worker_tag"

// Backup worker info
const val BACKUP_FOLDER_WORK_NAME = "backup_folder_work"
const val BACKUP_FOLDER_TAG = "backup_folder_tag"

// Sync worker info
const val SYNC_FOLDER_TAG = "sync_folder_tag"

// Keys for the backup and sync work
const val SMB_ID_INT_KEY = "SMB_ID"

const val MAX_RETRY_ATTEMPT = 3
// END: Worker related data

// START: Broadcast receiver related data
const val CANCEL_ACTION_KEY = "cancel_worker_tag"
const val CANCEL_ACTION_NAME = "cancel_upload"
// END: Broadcast receiver related data

const val NULL_ICON = 0
fun createCancelIntent(ctx: Context, pendingIntentKeyValue: String): PendingIntent {
    val cancelUploadIntent = Intent(ctx, UploadCancelReceiver::class.java).apply {
        action = CANCEL_ACTION_NAME
        putExtra(CANCEL_ACTION_KEY, pendingIntentKeyValue)
    }

    // Unique requestCode is required for reflecting the current worker tag.
    val cancelUploadPendingIntent =
        PendingIntent.getBroadcast(ctx, if (pendingIntentKeyValue == BACKUP_FOLDER_TAG) 0 else 1, cancelUploadIntent, PendingIntent.FLAG_IMMUTABLE)
    return cancelUploadPendingIntent

}

fun makeStatusNotification(
    message: String,
    ctx: Context,
    pendingIntentKeyValue: String? = null,
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

        pendingIntentKeyValue?.let { key ->
            builder.addAction(NULL_ICON, ctx.getString(R.string.cancel), createCancelIntent(ctx, key)) // 0 implies no icon provided
        }

        // Show the notification
        if (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, builder.build())
        }
    }
}

/**
 * Silently updates the main Notification channel
 */
fun updateNotificationMessage(
    message: String,
    ctx: Context,
    pendingIntentKeyValue: String? = null,
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

        pendingIntentKeyValue?.let { key ->
            builder.addAction(NULL_ICON, ctx.getString(R.string.cancel), createCancelIntent(ctx, pendingIntentKeyValue))
        }
        // Show the notification
        if (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, builder.build())
        }
    }
}