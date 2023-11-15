package com.ad.backupfiles.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ad.backupfiles.R

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

const val MAIN_CHANNEL_ID = "MAIN_BACKUP_NOTIFICATION"
const val MAIN_CHANNEL_NAME = "Main Backup Information"
const val MAIN_CHANNEL_DESC = "Main Backup Information"
const val DETAILED_CHANNEL_ID = "DETAIL_BACKUP_NOTIFICATION"
const val DETAILED_CHANNEL_NAME = "Detailed Backup Information"
const val DETAILED_CHANNEL_DESC = "Displays Detailed Live Backup Information"

const val NOTIFICATION_ID = 1
val BACKUP_NOTIFICATION_TITLE: CharSequence = "Backing Up Data"
val SYNC_NOTIFICATION_TITLE: CharSequence = "Syncing Data"


// Backup worker info
const val BACKUP_FOLDER_WORK_NAME = "backup_folder_work"
const val BACKUP_FOLDER_TAG = "backup_folder_tag"

// Sync worker info
const val SYNC_FOLDER_WORK_NAME = "sync_folder_work"
const val SYNC_FILE_TAG = "sync_folder_tag"

// Keys for the backup and sync work
const val SMB_ID_INT_KEY = "SMB_ID"
const val IS_SYNC_DIR_KEY = "IS_SYNC"

const val MAX_RETRY_ATTEMPT = 3

fun makeStatusNotification(message: String, ctx: Context, title: CharSequence = BACKUP_NOTIFICATION_TITLE) {
    // Check notification is enabled before creating it.
    if (NotificationManagerCompat.from(ctx).areNotificationsEnabled()) {
        val builder = NotificationCompat.Builder(ctx, MAIN_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))

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
fun updateNotificationMessage(message: String, ctx: Context, title: CharSequence = BACKUP_NOTIFICATION_TITLE) {
    // Check notification is enabled before creating it.
    if (NotificationManagerCompat.from(ctx).areNotificationsEnabled()) {
        val builder = NotificationCompat.Builder(ctx, DETAILED_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVibrate(LongArray(0))

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