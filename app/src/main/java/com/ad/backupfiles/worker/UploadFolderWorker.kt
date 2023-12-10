package com.ad.backupfiles.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.ad.backupfiles.AppEntryPoint
import com.ad.backupfiles.data.entity.SmbServerDto
import com.ad.backupfiles.data.entity.toDto
import com.ad.backupfiles.smb.api.SMBClientApi
import com.hierynomus.mserref.NtStatus
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.smbj.common.SMBRuntimeException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeoutException

private val TAG = UploadFolderWorker::class.java.simpleName

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */
class UploadFolderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    // These variables are initialized once and reused during retry

    private val directoryRepo = AppEntryPoint.appModule.directoryInfoApi
    private val smbServerRepo = AppEntryPoint.appModule.smbServerApi
    private val UNKNOWN_ID: Long = -1L
    private lateinit var notificationTitle: CharSequence
    private var smbClientApi: SMBClientApi = AppEntryPoint.appModule.smbClientApi

    override suspend fun doWork(): Result = coroutineScope {
        Log.d(TAG, "Worker started!")

        val workerTag = inputData.getString(WORKER_TAG)!!
        val isSync = workerTag == SYNC_FOLDER_TAG
        notificationTitle = if (isSync) SYNC_NOTIFICATION_TITLE else BACKUP_NOTIFICATION_TITLE
        if (runAttemptCount >= MAX_RETRY_ATTEMPT) {
            makeNotification(
                message = if (isSync) "Unable to sync data, retry shortly" else "Unable to backup data, retry shortly",
                ctx = AppEntryPoint.appModule.appContext,
                pendingIntentKeyValue = workerTag,
                notificationTitle = notificationTitle,
            )
            return@coroutineScope Result.failure()
        }
        val smbId = inputData.getLong(SMB_ID_INT_KEY, UNKNOWN_ID)
        if (smbId == UNKNOWN_ID) {
            return@coroutineScope Result.failure()
        }

        val smbDto: SmbServerDto = smbServerRepo.getSmbServer(smbId).toDto()
        var workResult: Result = Result.success()
        try {
            directoryRepo.getPendingSyncDirectories(smbId).cancellable().onStart {
                updateNotificationMessage(
                    message = if (isSync) "Sync started" else "Backup started",
                    pendingIntentKeyValue = workerTag,
                    ctx = AppEntryPoint.appModule.appContext,
                    notificationTitle = notificationTitle,
                )
            }.onCompletion { failure: Throwable? ->
                if (failure == null) {
                    updateNotificationMessage(
                        message = if (isSync) "Sync successful" else "Backup successful",
                        ctx = AppEntryPoint.appModule.appContext,
                        notificationTitle = notificationTitle,
                    )
                } else if (failure is CancellationException) {
                    Log.d(TAG, "onCompletion else: $failure")
                    directoryRepo.deleteAllPendingSyncDirectories(smbId)
                }
            }.collect { dirToSync ->
                Log.d(TAG, "doWork: Before launch")
                smbClientApi.saveFolder(AppEntryPoint.appModule.appContext, smbDto, dirToSync.dirPath, isSync)
                directoryRepo.processSyncedDirectory(dirToSync)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            processException(e).let { processedExc ->
                updateNotificationMessage(
                    message = processedExc.message,
                    ctx = AppEntryPoint.appModule.appContext,
                    notificationTitle = notificationTitle,
                )
                workResult = processedExc.result
            }
        }
        return@coroutineScope workResult
    }
}

/**
 * Processes the given exception and returns an [ExceptionResult].
 *
 * This function handles the provided exception and encapsulates the result
 * along with a detailed error message intended for display to the user in an
 * [ExceptionResult] data class.
 *
 * @param e The exception to be processed.
 * @return An [ExceptionResult] containing the result and a detailed error message.
 */
private fun processException(e: Exception): ExceptionResult {
    when (e) {
        is TimeoutException -> {
            Log.d(TAG, "Connection timeout!", e)
            return ExceptionResult(result = Result.failure(), message = "Backup failed, retry shortly.")
        }

        is ConnectException -> {
            if (e.localizedMessage?.contains("EHOSTUNREACH") == true) {
                Log.d(TAG, "Incorrect IP address of backup server", e)
                return ExceptionResult(result = Result.failure(), message = "Backup server not found or it is offline")
            }
            return ExceptionResult(result = Result.failure(), message = "Backup server offline.")
        }

        is SocketTimeoutException -> {
            Log.d(TAG, "SMB server connection timeout", e)
            return ExceptionResult(result = Result.failure(), message = "Backup server offline.")
        }

        is UnknownHostException -> return ExceptionResult(result = Result.failure(), message = "Backup server not found")

        is SMBApiException -> {
            if (e.statusCode == NtStatus.STATUS_SHARING_VIOLATION.value) {
                Log.d(TAG, "Failed to a create file, opened file on SMB server must first be closed.", e)
                return ExceptionResult(
                    result = Result.failure(),
                    message = "Please close files open from SMB server.",
                )
            }
            return ExceptionResult(result = Result.failure(), message = "Unable to backup the folder")
        }

        is SMBRuntimeException -> {
            var cause = e.cause
            while (cause != null) {
                if (TimeoutException::class.java.isInstance(cause)) {
                    Log.e(TAG, "Timeout while trying to connect with SMB server", e)
                    return ExceptionResult(result = Result.failure(), message = "Connection issue, retry shortly.")
                }
                cause = cause.cause
            }
            return ExceptionResult(result = Result.failure(), message = "Error backing up, retry shortly.")
        }

        else -> {
            Log.d(TAG, "Unknown error. Unable to backup given folder", e)
            return ExceptionResult(result = Result.failure(), message = "Unable to backup the folder.")
        }
    }
}

/**
 * Represents the result of processing an exception.
 *
 * @property result The result of the exception processing.
 * @property message A detailed message describing the error, intended for display to the user.
 */
private data class ExceptionResult(val result: Result, val message: String)
