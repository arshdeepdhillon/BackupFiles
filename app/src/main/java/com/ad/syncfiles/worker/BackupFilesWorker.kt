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

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

//TODO make BackupFilesWorker upload files in batches

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */
class BackupFilesWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    private val TAG = BackupFilesWorker::class.java.simpleName
    private val appContainer = AppDataContainer(applicationContext)
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Worker started!")
            val appCtx = applicationContext

            val smbClientWrapper = SMBClientWrapper()
            val dirUriInput = inputData.getString(DIR_URI_KEY) ?: return@withContext Result.failure()
            val dirToSaveUri: Uri = Uri.parse(dirUriInput)

            val smbServerId = inputData.getString(SMB_SERVER_KEY) ?: return@withContext Result.failure()

            val smbDto: SmbServerDto = appContainer.smbServerRepository
                .getSmbServer(smbServerId.toInt())
                .toDto()
            try {
                makeStatusNotification("Backup started", appCtx)
                smbClientWrapper.saveFolder(appCtx, smbDto, dirToSaveUri)
                makeStatusNotification("Backup successful", appCtx)
                return@withContext Result.success()
            } catch (e: Exception) {
                return@withContext handleException(TAG, e, appCtx, dirToSaveUri)
            }
        }
    }
}