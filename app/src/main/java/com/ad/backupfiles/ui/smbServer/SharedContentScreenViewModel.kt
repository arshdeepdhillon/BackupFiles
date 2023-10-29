package com.ad.backupfiles.ui.smbServer

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest.Companion.MIN_BACKOFF_MILLIS
import androidx.work.workDataOf
import com.ad.backupfiles.data.entity.DirectoryDto
import com.ad.backupfiles.data.entity.DirectoryInfo
import com.ad.backupfiles.data.entity.toDto
import com.ad.backupfiles.data.repository.SaveDirectoryRepository
import com.ad.backupfiles.worker.BACK_UP_FOLDERS_TAG
import com.ad.backupfiles.worker.BACK_UP_FOLDERS_WORK_NAME
import com.ad.backupfiles.worker.BackupFolderWorker
import com.ad.backupfiles.worker.DIR_URI_KEY
import com.ad.backupfiles.worker.SMB_SERVER_KEY
import com.ad.backupfiles.worker.SYNC_DIR_KEY
import com.ad.backupfiles.worker.SYNC_FILES_TAG
import com.ad.backupfiles.worker.SYNC_FOLDERS_WORK_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */


/**
 * Displays content from SMB server.
 */
class SharedContentScreenViewModel(
    stateHandle: SavedStateHandle,
    private val saveDirRepo: SaveDirectoryRepository,
    private val appContext: Context,
) : ViewModel() {

    private val smbServerId: Int = checkNotNull(stateHandle[SharedContentScreenDestination.argKey])
    private val workManager = WorkManager.getInstance(appContext)
    private var selectedFolders = mutableSetOf<Uri>()

    /**
     * Holds current UI state
     */
    var uiState: StateFlow<SMBContentUiState> =
        saveDirRepo.getAllSavedDirectoriesStream(smbServerId).filterNotNull().map { smbServerWithSavedDir ->
            val content = smbServerWithSavedDir.savedDirs.mapNotNull { dir ->
                val doc = DocumentFile.fromTreeUri(appContext, Uri.parse(dir.dirPath))
                doc?.name?.let {
                    dir.toDto(
                        dirUri = doc.uri,
                        dirName = it,
                        lastModified = doc.lastModified(),
                        itemCount = doc.listFiles().size,
                        isDirectory = doc.isDirectory
                    )
                }
            }.toList()
            SMBContentUiState(content = content)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = SMBContentUiState()
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
        private const val DOWNLOAD_PROVIDER_URI =
            "content://com.android.providers.downloads.documents/tree/downloads"
        private const val DOWNLOAD_EXT_URI =
            "content://com.android.externalstorage.documents/tree/primary%3ADownload"

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
            runBackupWorker(contentUri)
            if (!saveDirRepo.isDirectorySaved(smbServerId, contentUri.toString())) {
                addBackupDirInfo(contentUri)
                return@withContext true
            }
            return@withContext true
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
        val backupWorker = OneTimeWorkRequestBuilder<BackupFolderWorker>()
            .setInputData(
                workDataOf(
                    DIR_URI_KEY to contentUri.toString(),
                    SMB_SERVER_KEY to smbServerId.toString()
                )
            )
            .addTag(BACK_UP_FOLDERS_TAG)
            .setBackoffCriteria(BackoffPolicy.LINEAR, MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()
        workManager.beginUniqueWork(
            BACK_UP_FOLDERS_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            backupWorker
        ).enqueue()
    }


    /**
     *
     */
    fun syncSelectedFolder() {
        if (selectedFolders.isNotEmpty()) {
            val backupWorker = OneTimeWorkRequestBuilder<BackupFolderWorker>()
                .setInputData(
                    workDataOf(
                        SMB_SERVER_KEY to smbServerId.toString(),
                        DIR_URI_KEY to selectedFolders.first().toString(),
                        SYNC_DIR_KEY to true
                    )
                )
                .addTag(SYNC_FILES_TAG)
                .setBackoffCriteria(BackoffPolicy.LINEAR, MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .build()
            workManager.beginUniqueWork(
                SYNC_FOLDERS_WORK_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                backupWorker
            ).enqueue()
        }
    }


    /**
     * Handles selected/unselected backed up folder.
     *
     * @param item A Pair representing selection status of the folder
     * @property Pair.first  folder is selected if true, otherwise it's unselected
     * @property Pair.second [DirectoryDto] contains details about the folder.
     */
    fun handleSelected(item: Pair<Boolean, DirectoryDto>) {
        if (item.first) {
            selectedFolders.add(item.second.dirUri)
        } else {
            selectedFolders.remove(item.second.dirUri)
        }
    }
}


data class SMBContentUiState(
    val content: List<DirectoryDto> = emptyList(),
)
