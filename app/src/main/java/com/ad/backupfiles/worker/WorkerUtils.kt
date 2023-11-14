package com.ad.backupfiles.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ListenableWorker
import com.ad.backupfiles.R
import com.ad.backupfiles.smb.FileUtils
import com.hierynomus.mserref.NtStatus
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.smbj.common.SMBRuntimeException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

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


/**
 * Handles exceptions that may occur during a specific operation.
 * @param tag For logging purposes.
 * @param e The exception that was caught and needs to be handled.
 * @param appCtx The Android application context.
 * @param dirPath The path of this directory.
 * @return A ListenableWorker.Result value indicating whether the operation should be retried or marked as a failure.
 */
internal fun handleException(
    tag: String,
    e: Exception,
    appCtx: Context,
    dirPath: String?,
    title: CharSequence,
): ListenableWorker.Result {
    when (e) {
        is TimeoutException -> {
            Log.w(tag, "Connection timeout!", e)
            updateNotificationMessage("Backup failed, we'll retry shortly.", appCtx, title)
            return ListenableWorker.Result.retry()
        }

        is ConnectException -> {
            Log.w(tag, "SMB server is offline", e)
            if (e.localizedMessage?.contains("EHOSTUNREACH") == true) {
                Log.e(tag, "Incorrect IP address of backup server", e)
                updateNotificationMessage("Backup server not found or it is offline", appCtx, title)
                return ListenableWorker.Result.failure()
            }
            updateNotificationMessage("Backup server offline, we'll retry shortly.", appCtx, title)
            return ListenableWorker.Result.retry()
        }

        is SocketTimeoutException -> {
            Log.w(tag, "SMB server connection timeout", e)
            updateNotificationMessage("Backup server offline, we'll retry shortly.", appCtx, title)
            return ListenableWorker.Result.retry()
        }

        is UnknownHostException -> {
            updateNotificationMessage("Backup server not found", appCtx, title)
            return ListenableWorker.Result.failure()
        }

        is SMBApiException -> {
            if (e.statusCode == NtStatus.STATUS_SHARING_VIOLATION.value) {
                Log.w(
                    tag,
                    "Failed to a create file, opened file on SMB server must first be closed.",
                    e
                )
                updateNotificationMessage(
                    "Please close all files from '${
                        FileUtils.getDirName(
                            appCtx,
                            dirPath!!
                        )
                    }' folder", appCtx
                )
                return ListenableWorker.Result.retry()
            }
            updateNotificationMessage(
                "Unable to backup '${FileUtils.getDirName(appCtx, dirPath!!)}'",
                appCtx
            )
            return ListenableWorker.Result.failure()
        }

        is SMBRuntimeException -> {
            Log.w(tag, "Unable to connect with SMB server", e.cause)
            var cause = e.cause
            while (cause != null) {
                if (TimeoutException::class.java.isInstance(cause)) {
                    Log.e(tag, "SMB client timeout", e)
                    updateNotificationMessage("Error backing up, we'll retry shortly.", appCtx, title)
                    return ListenableWorker.Result.retry()
                }
                cause = cause.cause
            }
            updateNotificationMessage("Error backing up, we'll retry shortly.", appCtx, title)
            return ListenableWorker.Result.retry()
        }

        else -> {
            Log.e(tag, "Unable to backup given folder", e)
            updateNotificationMessage(
                "Unable to backup '${FileUtils.getDirName(appCtx, dirPath!!)}'.",
                appCtx
            )
            return ListenableWorker.Result.failure()
        }
    }
}


