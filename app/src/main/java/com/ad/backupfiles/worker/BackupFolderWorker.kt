package com.ad.backupfiles.worker


import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.ad.backupfiles.data.AppDataContainer
import com.ad.backupfiles.data.entity.SmbServerDto
import com.ad.backupfiles.data.entity.toDto
import com.ad.backupfiles.smb.SMBClientWrapper
import com.hierynomus.mserref.NtStatus
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.smbj.common.SMBRuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeoutException

private val TAG = BackupFolderWorker::class.java.simpleName

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */
class BackupFolderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    // These variables are initialized once and reused during retry

    private val directoryRepo = AppDataContainer(applicationContext).directoryRepo
    private val smbServerRepo = AppDataContainer(applicationContext).smbServerRepo
    private val UNKNOWN_ID: Long = -1L
    private lateinit var notificationTitle: CharSequence

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Worker started!")
            val isSync = inputData.getBoolean(IS_SYNC_DIR_KEY, false)
            notificationTitle = if (isSync) SYNC_NOTIFICATION_TITLE else BACKUP_NOTIFICATION_TITLE
            if (runAttemptCount >= MAX_RETRY_ATTEMPT) {
                makeStatusNotification(
                    if (isSync) "Unable to sync data, retry shortly" else "Unable to backup data, retry shortly",
                    applicationContext,
                    notificationTitle
                )
                return@withContext Result.failure()
            }
            val smbId = inputData.getLong(SMB_ID_INT_KEY, UNKNOWN_ID)
            if (smbId == UNKNOWN_ID) return@withContext Result.failure()
            val smbDto: SmbServerDto = smbServerRepo.getSmbServer(smbId).toDto()

            try {
                directoryRepo.getPendingSyncDirectories(smbId).cancellable().onStart {
                    makeStatusNotification(if (isSync) "Sync started" else "Backup started", applicationContext, notificationTitle)
                }.onCompletion {
                    if (it == null) {
                        updateNotificationMessage(if (isSync) "Sync successful" else "Backup successful", applicationContext, notificationTitle)
                    }
                }.collect { dirToSync ->
                    SMBClientWrapper().saveFolder(applicationContext, smbDto, dirToSync.dirPath, isSync)
                    directoryRepo.processSyncedDirectory(dirToSync)
                }
                return@withContext Result.success()
            } catch (e: Exception) {
                // Work was cancelled, propagate this up to parent so coroutine can be cancelled properly.
                if (e is CancellationException) throw e

                val processedException: ExceptionResult = processException(e)
                updateNotificationMessage(processedException.message, applicationContext, notificationTitle)
                return@withContext processedException.result
            }
        }
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
            Log.w(TAG, "Connection timeout!", e)
            return ExceptionResult(result = Result.retry(), message = "Backup failed, we'll retry shortly.")
        }

        is ConnectException -> {
            Log.w(TAG, "SMB server is offline", e)
            if (e.localizedMessage?.contains("EHOSTUNREACH") == true) {
                Log.e(TAG, "Incorrect IP address of backup server", e)
                return ExceptionResult(result = Result.failure(), message = "Backup server not found or it is offline")
            }
            return ExceptionResult(result = Result.retry(), message = "Backup server offline, we'll retry shortly.")
        }

        is SocketTimeoutException -> {
            Log.w(TAG, "SMB server connection timeout", e)
            return ExceptionResult(result = Result.retry(), message = "Backup server offline, we'll retry shortly.")
        }

        is UnknownHostException -> return ExceptionResult(result = Result.failure(), message = "Backup server not found")

        is SMBApiException -> {
            if (e.statusCode == NtStatus.STATUS_SHARING_VIOLATION.value) {
                Log.w(TAG, "Failed to a create file, opened file on SMB server must first be closed.", e)
                return ExceptionResult(
                    result = Result.retry(),
                    message = "Please close files open from SMB server."
                )
            }
            return ExceptionResult(result = Result.failure(), message = "Unable to backup the folder")
        }

        is SMBRuntimeException -> {
            var cause = e.cause
            while (cause != null) {
                if (TimeoutException::class.java.isInstance(cause)) {
                    Log.e(TAG, "Timeout while trying to connect with SMB server", e)
                    break
                }
                cause = cause.cause
            }
            return ExceptionResult(result = Result.retry(), message = "Error backing up, we'll retry shortly.")
        }

        else -> {
            Log.e(TAG, "Unknown error. Unable to backup given folder", e)
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