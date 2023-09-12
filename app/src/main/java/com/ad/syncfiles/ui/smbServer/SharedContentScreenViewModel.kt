package com.ad.syncfiles.ui.smbServer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ad.syncfiles.data.repository.SmbServerInfoRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Displays content from SMB server.
 */
class SharedContentScreenViewModel(
    stateHandle: SavedStateHandle,
    private val serverInfoRepo: SmbServerInfoRepository,
) :
    ViewModel() {

    private val TAG: String = "SharedContentScreenViewModel"
    private val smbServerId: Int = checkNotNull(stateHandle[EditScreenDestination.argKey])

    /**
     * Holds current UI state
     */
    var uiState by mutableStateOf(DeviceDetailsUiState())
        private set

    init {
        viewModelScope.launch {
            uiState = serverInfoRepo.getSmbServerStream(smbServerId).filterNotNull().first().toUiState(true)
        }
    }

    /**
     * Update the item in the [ItemsRepository]'s data source
     */
    suspend fun updateItem() {
        if (validateInput(uiState.deviceDetails)) {
            serverInfoRepo.upsertSmbServer(uiState.deviceDetails.toSmbServerInfo())
        }
    }

    /**
     * Updates the [uiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(deviceDetails: SharedDeviceDetails) {
        uiState =
            DeviceDetailsUiState(deviceDetails = deviceDetails, isEntryValid = validateInput(deviceDetails))
    }


    private fun validateInput(deviceDetails: SharedDeviceDetails = uiState.deviceDetails): Boolean {
        return with(deviceDetails) {
            serverUrl.isNotBlank()
        }
    }


}
