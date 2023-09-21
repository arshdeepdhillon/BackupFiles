package com.ad.syncfiles.ui.smbServer

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ad.syncfiles.data.entity.DirectoryInfo
import com.ad.syncfiles.data.repository.SaveDirectoryRepository
import com.ad.syncfiles.data.repository.SmbServerInfoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Displays content from SMB server.
 */
class SharedContentScreenViewModel(
    stateHandle: SavedStateHandle,
    private val serverInfoRepo: SmbServerInfoRepository,
    private val saveDirRepo: SaveDirectoryRepository,
) : ViewModel() {

    private val smbServerId: Int = checkNotNull(stateHandle[SharedContentScreenDestination.argKey])

    /**
     * Holds current UI state
     */
    var uiState: StateFlow<SMBContentUiState> = saveDirRepo.getAllSavedDirectoriesStream(smbServerId).filterNotNull().map { dirs ->

        SMBContentUiState(
            dirs.savedDirs.map { dir ->
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

    suspend fun addBackupDirInfo(contentUri: Uri) {
        val contentUriToSave = when (contentUri.authority) {
            null -> contentUri
            else -> PROVIDER_TO_EXT.getOrDefault(contentUri.toString(), contentUri)
        }.toString()

        val dir = DirectoryInfo(smbServerId = smbServerId, dirPath = contentUriToSave)
        saveDirRepo.upsertDirectory(dir)
    }
}

data class SMBContentUiState(
    /**
     * List of
     */
    val content: List<String> = emptyList(),
)
