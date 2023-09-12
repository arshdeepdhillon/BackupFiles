package com.ad.syncfiles.ui.systemFolders

import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ad.syncfiles.data.repository.SmbServerInfoRepository
import java.io.File
import java.util.ArrayDeque

/**
 * ViewModel to retrieve all the content from Internal/External storage.
 */
class MediaBrowserViewModel(stateHandle: SavedStateHandle, private val smbServerRepository: SmbServerInfoRepository) : ViewModel() {

    private val queue = ArrayDeque<String>()

    private val smbServerId: Int = checkNotNull(stateHandle[MediaBrowserScreenDestination.argKey])

    var uiState by mutableStateOf(SystemFiles())
        private set

    companion object {
        private val rootUri: File = Environment.getRootDirectory()
        private val sdCardUri: File = Environment.getExternalStorageDirectory()
    }

    init {
        uiState = SystemFiles(mediaFiles = listOf(rootUri, sdCardUri))
    }

    suspend fun saveSelectedBackupFolder() {
//        queue.peekLast()?.let { smbServerRepository.addBackupDirPath(smbServerId, it) }
    }
//    // Request code for creating a PDF document.
//    val CREATE_FILE = 1
//    fun openDirectory() {
//        val pickerInitialUri = queue.peekLast()
//        // Choose a directory using the system's file picker.
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
//            // Optionally, specify a URI for the directory that should be opened in
//            // the system file picker when it loads.
//            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
//        }
//
//        startActivityForResult(intent, CREATE_FILE)
//    }

    fun onItemClick(file: File) {
        val files = file.listFiles()
        if (file.isDirectory) {
            queue.add(file.path)
            uiState = if (files == null) {
                SystemFiles()
            } else {
                SystemFiles(mediaFiles = files.toList())
            }
        }
    }

    /**
     * TODO
     */
//    fun handleBackButton(navigateBack: () -> Unit): () -> Unit {
//        val dirPath = queue.removeLastOrNull()
//        if (dirPath == null) {
//            return navigateBack
//        } else {
//            onItemClick(File(dirPath))
//        }
//        return {}
//    }
}

/**
 * Ui state for the Home screen
 */
data class SystemFiles(val mediaFiles: List<File> = listOf())