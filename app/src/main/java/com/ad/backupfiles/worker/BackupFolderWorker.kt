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
import kotlinx.coroutines.withContext

//TODO make BackupFilesWorker upload files in batches

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */
class BackupFolderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    private val TAG = BackupFolderWorker::class.java.simpleName
    private val appContainer = AppDataContainer(applicationContext)
    private val UNKNOWN_ID = -1L

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Worker started!")
            val isSync = inputData.getBoolean(SYNC_DIR_KEY, false)
            if (runAttemptCount >= MAX_RETRY_ATTEMPT) {
                makeStatusNotification(
                    if (isSync) "Unable to sync data, retry shortly" else "Unable to backup data, retry shortly",
                    applicationContext
                )
                return@withContext Result.failure()
            }
            return@withContext processWork(isSync)
        }
    }

    private suspend fun processWork(isSync: Boolean): Result {
        var pathOfDir: String? = null
        try {
            val dirId = inputData.getLong(DIR_ID_LONG_KEY, UNKNOWN_ID)
            if (dirId == UNKNOWN_ID) return Result.failure()

            val smbId = inputData.getInt(SMB_ID_INT_KEY, UNKNOWN_ID.toInt())
            if (smbId == UNKNOWN_ID.toInt()) return Result.failure()

            val dirDto = appContainer.directoryRepo.getDir(dirId, smbId) ?: return Result.failure()
            pathOfDir = dirDto.dirPath
            val smbDto: SmbServerDto = appContainer.smbServerRepo.getSmbServer(smbId).toDto()

            val smbClientWrapper = SMBClientWrapper()
            makeStatusNotification(if (isSync) "Sync started" else "Backup started", applicationContext)
            smbClientWrapper.saveFolder(applicationContext, smbDto, pathOfDir, isSync)
            makeStatusNotification(if (isSync) "Sync successful" else "Backup successful", applicationContext)
            appContainer.directoryRepo.updateSyncTime(dirId, smbId)
            return Result.success()
        } catch (e: Exception) {
            return handleException(TAG, e, applicationContext, pathOfDir)
        }
    }
}