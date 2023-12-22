package com.ad.backupfiles.ui.savedDirectoriesScreen

import android.net.Uri
import androidx.annotation.StringRes
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest.Companion.MIN_BACKOFF_MILLIS
import androidx.work.workDataOf
import com.ad.backupfiles.R
import com.ad.backupfiles.data.entity.DirectoryDto
import com.ad.backupfiles.data.entity.DirectoryInfo
import com.ad.backupfiles.data.entity.toDto
import com.ad.backupfiles.di.api.ApplicationModuleApi
import com.ad.backupfiles.worker.BACKUP_FOLDER_TAG
import com.ad.backupfiles.worker.BACKUP_FOLDER_WORK_NAME
import com.ad.backupfiles.worker.SMB_ID_INT_KEY
import com.ad.backupfiles.worker.SYNC_FOLDER_TAG
import com.ad.backupfiles.worker.UploadFolderWorker
import com.ad.backupfiles.worker.WORKER_TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Displays saved directories for the selected SMB server.
 */
class SavedDirectoriesViewModel(
    @Suppress("unused") private val stateHandle: SavedStateHandle,
    private val appModuleApi: ApplicationModuleApi,
) : ViewModel() {

    private val smbServerId: Long =
        checkNotNull(stateHandle[SavedDirectoriesScreenDestination.argKey])
    private val workManager = WorkManager.getInstance(appModuleApi.appContext)
    private val workManagerConstraints =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    private val _errorState = MutableSharedFlow<ErrorUiState>()
    val errorState: SharedFlow<ErrorUiState> = _errorState.asSharedFlow()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS))

    /**
     * Holds current UI state
     */
    var uiState: StateFlow<SMBContentUiState> =
        appModuleApi.directoryInfoApi.getAllSavedDirectoriesStream(smbServerId).filterNotNull()
            .map { smbServerWithSavedDir ->
                SMBContentUiState(savedDirectories = smbServerWithSavedDir.savedDirs.map { it.toDto() })
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = SMBContentUiState(),
            )

    sealed class ErrorUiState {
        /* Note: If using the same object without changing its contents, StateFlow will not emit.*/
        data class Error(@StringRes val resId: Int, val args: List<String?> = emptyList()) :
            ErrorUiState()

        object Empty : ErrorUiState()
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    /**
     * Saves the [persistableDirUri] folder path so it can later be backed up to the SMB server.
     *
     * @param persistableDirUri The Uri representing the directory to be saved.
     * @return `true` if the directory was successfully saved, `false` otherwise.
     */
    fun saveDirectory(persistableDirUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val dirName: String? =
                DocumentFile.fromTreeUri(appModuleApi.appContext, persistableDirUri)?.name
            if (appModuleApi.directoryInfoApi.isDirectorySaved(smbServerId, persistableDirUri.toString())) {
                _errorState.emit(
                    ErrorUiState.Error(
                        resId = R.string.error_folder_already_saved,
                        args = listOf(dirName),
                    ),
                )
                return@launch
            }
            val savedDirId: Long? = save(persistableDirUri)
            if (savedDirId == null) {
                _errorState.emit(
                    ErrorUiState.Error(
                        resId = R.string.generic_error_failed_to_save_folder,
                        args = listOf(dirName),
                    ),
                )
                return@launch
            }
            runBackupWorker(savedDirId)
        }
    }

    /**
     * Save the directory using the given [dirToSaveUri].
     *
     * @param dirToSaveUri The Uri mapping to directory to be saved.
     * @return [Long] id of the saved directory or null if Uri is invalid.
     */
    private suspend fun save(dirToSaveUri: Uri): Long? {
        return DocumentFile.fromTreeUri(appModuleApi.appContext, dirToSaveUri)?.let { doc ->
            return appModuleApi.directoryInfoApi.insertDirectory(
                DirectoryInfo(
                    smbServerId = smbServerId,
                    dirPath = dirToSaveUri.toString(),
                    dirName = doc.name,
                ),
            )
        }
    }

    /**
     * Queues the backup for the specified directory ID using WorkManager.
     * @param dirId The ID of the directory to be backed up.
     */
    private fun runBackupWorker(dirId: Long) {
        viewModelScope.launch {
            appModuleApi.directoryInfoApi.insertDirectoriesToSync(smbServerId, mutableListOf(dirId))
            val backupWork = OneTimeWorkRequestBuilder<UploadFolderWorker>()
                .addTag(BACKUP_FOLDER_TAG)
                .setConstraints(workManagerConstraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(SMB_ID_INT_KEY to smbServerId, WORKER_TAG to BACKUP_FOLDER_TAG),
                )
                .build()
            workManager.beginUniqueWork(
                BACKUP_FOLDER_WORK_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                backupWork,
            ).enqueue()
        }
    }

    /**
     * Queues synchronization work for the selected directories.
     *
     * This function initiates the synchronization process for the provided list of directory IDs.
     * It checks if the list is not empty, then launches a coroutine within the ViewModel's scope
     * to perform the following tasks:
     *
     * 1. Persists the selected directories to be synchronized by inserting into database.
     *
     * 2. Enqueues a OneTimeWorkRequest to perform the synchronization work asynchronously using WorkManager.
     *
     * @param directoryIdsToSync A MutableList of Long representing the IDs of selected directories
     *                            to be synchronized.
     */
    fun queueSyncWork(directoryIdsToSync: MutableList<Long>) {
        if (directoryIdsToSync.isNotEmpty()) {
            viewModelScope.launch {
                appModuleApi.directoryInfoApi.insertDirectoriesToSync(
                    smbServerId,
                    directoryIdsToSync,
                )
                val syncWork = OneTimeWorkRequestBuilder<UploadFolderWorker>()
                    .addTag(SYNC_FOLDER_TAG)
                    .setInputData(
                        workDataOf(
                            SMB_ID_INT_KEY to smbServerId,
                            WORKER_TAG to SYNC_FOLDER_TAG,
                        ),
                    )
                    .setConstraints(workManagerConstraints)
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS,
                    )
                    .build()

                workManager.beginUniqueWork(
                    BACKUP_FOLDER_WORK_NAME,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    syncWork,
                ).enqueue()
            }
        }
    }
}

/**
 * Represents the UI state of saved directories.
 *
 * @property savedDirectories List of [DirectoryDto] to be displayed.
 */
data class SMBContentUiState(
    val savedDirectories: List<DirectoryDto> = emptyList(),
)
