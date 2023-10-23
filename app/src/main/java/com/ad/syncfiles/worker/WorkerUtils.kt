package com.ad.syncfiles.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ListenableWorker
import com.ad.syncfiles.R
import com.ad.syncfiles.smb.FileUtils
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

const val CHANNEL_ID = "BACKUP_NOTIFICATION"
const val CHANNEL_NAME = "Backup Information"
const val CHANNEL_DESC = "Displays Live Backup Information"

const val NOTIFICATION_ID = 1
val NOTIFICATION_TITLE: CharSequence = "Backing Up Data"


// The name of the backup work
const val BACK_UP_FILES_WORK_NAME = "backup_files_work"
const val BACK_UP_FILES_TAG = "backup_files_tag"

// Keys for the backup work
const val DIR_URI_KEY = "DIR_URI"
const val SMB_SERVER_KEY = "SMB_SERVER_ID"

fun makeStatusNotification(message: String, ctx: Context) {
    // Check notification is enabled before creating it.
    if (NotificationManagerCompat.from(ctx).areNotificationsEnabled()) {
        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))

        // Show the notification
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, builder.build())
        }
    }
}


/**
 * Handles exceptions that may occur during a specific operation.
 *
 * @param e The exception that was caught and needs to be handled.
 * @param appCtx The Android application context.
 * @param uri The URI associated with the operation.
 * @return A ListenableWorker.Result value indicating whether the operation should be retried or marked as a failure.
 */
internal fun handleException(TAG: String, e: Exception, appCtx: Context, uri: Uri): ListenableWorker.Result {
    when (e) {
        is TimeoutException -> {
            Log.w(TAG, "Connection timeout!", e)
            makeStatusNotification("Backup failed, we'll retry shortly.", appCtx)
            return ListenableWorker.Result.retry()
        }

        is ConnectException -> {
            Log.w(TAG, "SMB server is offline", e)
            if (e.localizedMessage?.contains("EHOSTUNREACH") == true) {
                Log.e(TAG, "Incorrect IP address of backup server", e)
                makeStatusNotification("Backup server does not exist", appCtx)
                return ListenableWorker.Result.failure()
            }
            makeStatusNotification("Backup server offline, we'll retry shortly.", appCtx)
            return ListenableWorker.Result.retry()
        }

        is SocketTimeoutException -> {
            Log.w(TAG, "SMB server connection timeout", e)
            makeStatusNotification("Backup server offline, we'll retry shortly.", appCtx)
            return ListenableWorker.Result.retry()
        }

        is UnknownHostException -> {
            makeStatusNotification("Backup server does not exist", appCtx)
            return ListenableWorker.Result.failure()
        }

        is SMBApiException -> {
            if (e.statusCode == NtStatus.STATUS_SHARING_VIOLATION.value) {
                Log.w(TAG, "Failed to a create file, opened file on SMB server must first be closed.", e)
                makeStatusNotification("Please close all files from '${FileUtils.getDirName(appCtx, uri)}' folder", appCtx)
                return ListenableWorker.Result.retry()
            }
            makeStatusNotification("Unable to backup '${FileUtils.getDirName(appCtx, uri)}'", appCtx)
            return ListenableWorker.Result.failure()
        }

        is SMBRuntimeException -> {
            Log.w(TAG, "Unable to connect with SMB server", e.cause)
            var cause = e.cause
            while (cause != null) {
                if (TimeoutException::class.java.isInstance(cause)) {
                    Log.e(TAG, "SMB client timeout", e)
                    makeStatusNotification("Error backing up, we'll retry shortly.", appCtx)
                    return ListenableWorker.Result.retry()
                }
                cause = cause.cause
            }
            makeStatusNotification("Error backing up, we'll retry shortly.", appCtx)
            return ListenableWorker.Result.retry()
        }

        else -> {
            Log.e(TAG, "Unable to backup given folder", e)
            makeStatusNotification("Unable to backup '${FileUtils.getDirName(appCtx, uri)}'.", appCtx)
            return ListenableWorker.Result.failure()
        }
    }
}


