package com.ad.syncfiles.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ad.syncfiles.smb.FileUtils
import com.ad.syncfiles.smb.SMB
import com.hierynomus.mserref.NtStatus
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.smbj.common.SMBRuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

// TODO make BackupFilesWorker run in foreground even when app is closed
// TODO make BackupFilesWorker upload files in batches

class BackupFilesWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    private val TAG = BackupFilesWorker::class.java.simpleName

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Worker started!")

            val smb = SMB() // Default timeout is 60 seconds
            val dirUriInput = inputData.getString("DIR_URI") ?: return@withContext Result.failure()
            val uri: Uri = Uri.parse(dirUriInput)
            val appCtx = applicationContext
            try {
                makeStatusNotification("Backup started", appCtx)
                smb.saveFolder(appCtx, uri)
                makeStatusNotification("Backup successful", appCtx)
                return@withContext Result.success()
            } catch (e: Exception) {
                return@withContext handleException(e, appCtx, uri)
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
    private fun handleException(e: Exception, appCtx: Context, uri: Uri): Result {
        when (e) {
            is TimeoutException -> {
                Log.w(TAG, "Connection timeout!", e)
                makeStatusNotification("Backup failed, we'll retry shortly.", appCtx)
                return Result.retry()
            }

            is ConnectException -> {
                Log.w(TAG, "SMB server is offline", e)
                makeStatusNotification("Backup server offline, we'll retry shortly.", appCtx)
                return Result.retry()
            }

            is SocketTimeoutException -> {
                Log.w(TAG, "SMB server connection timeout", e)
                makeStatusNotification("Backup server offline, we'll retry shortly.", appCtx)
                return Result.retry()
            }

            is SMBApiException -> {
                if (e.statusCode == NtStatus.STATUS_SHARING_VIOLATION.value) {
                    Log.w(TAG, "Failed to a create file, opened file on SMB server must first be closed.", e)
                    makeStatusNotification("Please close all files from '${FileUtils.getDirName(appCtx, uri)}' folder", appCtx)
                    return Result.retry()
                }
                makeStatusNotification("Unable to backup '${FileUtils.getDirName(appCtx, uri)}'", appCtx)
                return Result.failure()
            }

            is SMBRuntimeException -> {
                Log.w(TAG, "Unable to connect with SMB server", e.cause)
                var cause = e.cause
                while (cause != null) {
                    if (TimeoutException::class.java.isInstance(cause)) {
                        Log.e(TAG, "SMB client timeout", e)
                        makeStatusNotification("Error backing up, we'll retry shortly.", appCtx)
                        return Result.retry()
                    }
                    cause = cause.cause
                }
                makeStatusNotification("Error backing up, we'll retry shortly.", appCtx)
                return Result.retry()
            }

            else -> {
                Log.e(TAG, "Unable to backup given folder", e)
                makeStatusNotification("Unable to backup '${FileUtils.getDirName(appCtx, uri)}'.", appCtx)
                return Result.failure()
            }
        }
    }
}