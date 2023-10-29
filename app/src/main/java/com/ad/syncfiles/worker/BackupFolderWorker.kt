package com.ad.syncfiles.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ad.syncfiles.data.AppDataContainer
import com.ad.syncfiles.data.entity.SmbServerDto
import com.ad.syncfiles.data.entity.toDto
import com.ad.syncfiles.smb.SMBClientWrapper
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
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val appCtx = applicationContext
            val isSync = inputData.getBoolean(SYNC_DIR_KEY, false)

            if (runAttemptCount >= MAX_RETRY_ATTEMPT) {
                makeStatusNotification(if (isSync) "Unable to sync data, retry shortly" else "Unable to backup data, retry shortly", appCtx)
                return@withContext Result.failure()
            }

            Log.d(TAG, "Worker started!")
            val dirUriInput = inputData.getString(DIR_URI_KEY) ?: return@withContext Result.failure()
            val smbClientWrapper = SMBClientWrapper()
            val dirToSaveUri: Uri = Uri.parse(dirUriInput)
            val smbServerId = inputData.getString(SMB_SERVER_KEY) ?: return@withContext Result.failure()

            val smbDto: SmbServerDto = appContainer.smbServerRepository
                .getSmbServer(smbServerId.toInt())
                .toDto()
            try {
                makeStatusNotification(if (isSync) "Sync started" else "Backup started", appCtx)
                smbClientWrapper.saveFolder(appCtx, smbDto, dirToSaveUri, isSync)
                makeStatusNotification(if (isSync) "Sync successful" else "Backup successful", appCtx)
                return@withContext Result.success()
            } catch (e: Exception) {
                return@withContext handleException(TAG, e, appCtx, dirToSaveUri)
            }
        }
    }
}