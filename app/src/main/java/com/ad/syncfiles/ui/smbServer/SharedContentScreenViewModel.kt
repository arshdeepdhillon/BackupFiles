package com.ad.syncfiles.ui.smbServer

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ad.syncfiles.data.entity.DirectoryInfo
import com.ad.syncfiles.data.repository.SaveDirectoryRepository
import com.ad.syncfiles.data.repository.SmbServerInfoRepository
import com.ad.syncfiles.smb.SMB
import com.ad.syncfiles.worker.BACK_UP_FILES_WORK_NAME
import com.ad.syncfiles.worker.BackupFilesWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

val TAG = "SharedContentScreenViewModel"

/**
 * Displays content from SMB server.
 */
class SharedContentScreenViewModel(
    stateHandle: SavedStateHandle,
    private val serverInfoRepo: SmbServerInfoRepository,
    private val saveDirRepo: SaveDirectoryRepository,
    appContext: Context,
) : ViewModel() {

    private val smbServerId: Int = checkNotNull(stateHandle[SharedContentScreenDestination.argKey])
    private val workManager = WorkManager.getInstance(appContext)
    private val smb = SMB()

    /**
     * Holds current UI state
     */
    var uiState: StateFlow<SMBContentUiState> = saveDirRepo.getAllSavedDirectoriesStream(smbServerId).filterNotNull().map { smbServerWithSavedDir ->

        SMBContentUiState(
            smbServerWithSavedDir.savedDirs.map { dir ->
                dir.dirPath
            }.toList()
        )
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = SMBContentUiState())

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
        private const val DOWNLOAD_PROVIDER_URI = "content://com.android.providers.downloads.documents/tree/downloads"
        private const val DOWNLOAD_EXT_URI = "content://com.android.externalstorage.documents/tree/primary%3ADownload"

        /** Maps __Provider__ content to __External storage__ */
        val PROVIDER_TO_EXT: Map<String, String> = mapOf(DOWNLOAD_PROVIDER_URI to DOWNLOAD_EXT_URI)
    }


    /**
     * Saves the [contentUri] folder path so it can later be backed up to the SMB server.
     *
     * @param contentUri The Uri representing the directory to be saved.
     * @return `true` if the directory was successfully saved, `false` otherwise.
     */
    internal suspend fun saveDirectory(contentUri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            if (!saveDirRepo.isDirectorySaved(smbServerId, contentUri.toString())) {
                addBackupDirInfo(contentUri)
                //runBackupWorker(contentUri)
                return@withContext true
            }
            return@withContext false
        }
    }

    private suspend fun addBackupDirInfo(contentUri: Uri) {
        val dirPath = when (contentUri.authority) {
            null -> contentUri
            else -> PROVIDER_TO_EXT.getOrDefault(contentUri.toString(), contentUri)
        }.toString()

        val dir = DirectoryInfo(smbServerId = smbServerId, dirPath = dirPath)
        saveDirRepo.upsertDirectory(dir)
    }

    private fun runBackupWorker(contentUri: Uri) {
        val backupWorker = OneTimeWorkRequestBuilder<BackupFilesWorker>()
//            .setInputData(getWorkData())
            .setInputData(workDataOf("DIR_URI" to contentUri.toString()))
            .addTag("backup_files_tag")
            .build()
        workManager.beginUniqueWork(
            BACK_UP_FILES_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            backupWorker
        ).enqueue()
    }


//    private fun getWorkData(): Data {
//        if (uiState.value.content.isNotEmpty()) {
//            Log.d(TAG, "getWorkData size: " + uiState.value.content.size.toString())
//            return workDataOf("DIR_URI" to uiState.value.content.first())
//        } else {
//            Log.d(TAG, "getWorkData size: empty")
//            return workDataOf()
//        }
//    }
}

data class SMBContentUiState(
    /**
     * List of
     */
    val content: List<String> = emptyList(),
)
