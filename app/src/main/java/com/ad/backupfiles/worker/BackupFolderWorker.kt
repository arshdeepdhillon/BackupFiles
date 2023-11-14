package com.ad.backupfiles.worker


import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ad.backupfiles.data.AppDataContainer
import com.ad.backupfiles.data.entity.SmbServerDto
import com.ad.backupfiles.data.entity.toDto
import com.ad.backupfiles.smb.SMBClientWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException


/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */
class BackupFolderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    private val TAG = BackupFolderWorker::class.java.simpleName
    private val directoryRepo = AppDataContainer(applicationContext).directoryRepo
    private val smbServerRepo = AppDataContainer(applicationContext).smbServerRepo
    private val UNKNOWN_ID: Long = -1L
    private lateinit var title: CharSequence
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Worker started!")
            val isSync = inputData.getBoolean(IS_SYNC_DIR_KEY, false)
            title = if (isSync) SYNC_NOTIFICATION_TITLE else BACKUP_NOTIFICATION_TITLE
            if (runAttemptCount >= MAX_RETRY_ATTEMPT) {
                makeStatusNotification(
                    if (isSync) "Unable to sync data, retry shortly" else "Unable to backup data, retry shortly",
                    applicationContext, title
                )
                return@withContext Result.failure()
            }
            val smbId = inputData.getLong(SMB_ID_INT_KEY, UNKNOWN_ID)
            if (smbId == UNKNOWN_ID) return@withContext Result.failure()
            val smbClientWrapper = SMBClientWrapper()
            val smbDto: SmbServerDto = smbServerRepo.getSmbServer(smbId).toDto()

            //TODO fix this!
            var currentDirPath: String = ""

            try {
                directoryRepo.getPendingSyncDirectories(smbId)
                    .cancellable()
                    .onStart {
                        makeStatusNotification(if (isSync) "Sync started" else "Backup started", applicationContext, title)
                    }.onCompletion {
                        if (it == null) {
                            updateNotificationMessage(if (isSync) "Sync successful" else "Backup successful", applicationContext, title)
                        }
                    }.collect { dirToSync ->
                        Log.d(TAG, "dirToSync.dirPath ${dirToSync.dirPath}")
                        currentDirPath = dirToSync.dirPath
                        smbClientWrapper.saveFolder(applicationContext, smbDto, currentDirPath, isSync)
                        directoryRepo.processSyncedDirectory(dirToSync)
                    }
                return@withContext Result.success()
            } catch (e: Exception) {
                if (e is CancellationException) {
                    Log.d(TAG, "Work cancelled!", e)
                    throw e
                }
                return@withContext handleException(TAG, e, applicationContext, currentDirPath, title)
            }
        }
    }
}