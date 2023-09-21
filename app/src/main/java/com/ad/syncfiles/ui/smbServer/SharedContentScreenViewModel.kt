package com.ad.syncfiles.ui.smbServer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {

    private val smbServerId: Int = checkNotNull(stateHandle[SharedContentScreenDestination.argKey])

    /**
     * Holds current UI state
     */
    var uiState: StateFlow<SMBContentUiState> = serverInfoRepo.getSmbServerStream(smbServerId).filterNotNull().map {
        SMBContentUiState(content = listOf(it.backupDirPath))
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), initialValue = SMBContentUiState())

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }


//    /**
//     * Update the item in the [ItemsRepository]'s data source
//     */
//    suspend fun updateItem() {
//        if (validateInput(uiState.deviceDetails)) {
//            serverInfoRepo.upsertSmbServer(uiState.deviceDetails.toSmbServerInfo())
//        }
//    }

    suspend fun addBackupDirInfo(contentUri: String) {
        serverInfoRepo.addBackupDirPath(smbServerId, contentUri)
    }

//    /**
//     * Updates the [uiState] with the value provided in the argument. This method also triggers
//     * a validation for input values.
//     */
//    fun updateUiState(deviceDetails: SharedDeviceDetails) {
//        uiState =
//            DeviceDetailsUiState(deviceDetails = deviceDetails, isEntryValid = validateInput(deviceDetails))
//    }

    //
//    private fun validateInput(deviceDetails: SharedDeviceDetails = uiState.deviceDetails): Boolean {
//        return with(deviceDetails) {
//            serverUrl.isNotBlank()
//        }
//    }


}

data class SMBContentUiState(
    val content: List<String> = emptyList(),
)
