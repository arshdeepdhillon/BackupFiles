package com.ad.backupfiles.ui.smbServer

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ad.backupfiles.data.entity.toUiState
import com.ad.backupfiles.data.repository.SmbServerInfoRepository
import com.ad.backupfiles.smb.SMBClientWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Updates the fields of an item from [SmbServerInfoRepository]'s data source
 */
class EditScreenViewModel(
    stateHandle: SavedStateHandle,
    private val serverInfoRepo: SmbServerInfoRepository,
) : ViewModel() {
    private val smbServerId: Int = checkNotNull(stateHandle[EditScreenDestination.argKey])

    private val TAG = EditScreenViewModel::class.java.simpleName
    private val smb = SMBClientWrapper()

    /**
     * Holds current UI state
     */
    var uiState by mutableStateOf(ServerInfoUiState())
        private set

    init {
        viewModelScope.launch {
            uiState = serverInfoRepo.getSmbServerStream(smbServerId).filterNotNull().first()
                .toUiState(true)
        }
    }

    /**
     * Update the item in the [ItemsRepository]'s data source
     */
    suspend fun updateItem() {
        if (validateInput(uiState.serverDetails)) {
            serverInfoRepo.upsertSmbServer(uiState.serverDetails.toSmbServerInfo())
        }
    }

    suspend fun testConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext if (smb.canConnect(uiState.toDto())) {
                Log.d(TAG, "Successfully connected with new changes")
                true
            } else {
                Log.e(TAG, "Unable to connect with SMB Server ${uiState.toDto()}")
                false
            }
        }
    }

    /**
     * Updates the [uiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(deviceDetails: ServerDetails) {
        uiState =
            ServerInfoUiState(serverDetails = deviceDetails, isValid = validateInput(deviceDetails))
    }


    private fun validateInput(deviceDetails: ServerDetails = uiState.serverDetails): Boolean {
        return with(deviceDetails) {
            serverAddress.isNotBlank()
        }
    }
}
