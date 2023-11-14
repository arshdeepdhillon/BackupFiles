package com.ad.backupfiles.ui.smbServer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ad.backupfiles.data.repository.SmbServerInfoRepo
import com.ad.backupfiles.smb.SMBClientWrapper
import com.ad.backupfiles.ui.utils.SMBServerUiState
import com.ad.backupfiles.ui.utils.SmbServerInfoUiData
import com.ad.backupfiles.ui.utils.sanitizeAndValidateInputFields
import com.ad.backupfiles.ui.utils.toSmbServerEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

class AddScreenViewModel(private val serverInfoRepo: SmbServerInfoRepo) : ViewModel() {
    private val TAG = AddScreenViewModel::class.java.simpleName

    private val smb = SMBClientWrapper()

    /**
     * Holds current UI state
     */
    private val _uiState = MutableStateFlow(SMBServerUiState())
    val uiState: StateFlow<SMBServerUiState> = _uiState.asStateFlow()

    var userInputState by mutableStateOf(SmbServerInfoUiData())
        private set


    /**
     * Syncs the UI state with the given [deviceDetails] and determines whether the input is valid.
     * Typically used to update the internal state in response to changes in [AddScreen].
     *
     * @param deviceDetails The server details to update the UI state with.
     */
    fun updateUiState(deviceDetails: SmbServerInfoUiData) {
        userInputState = deviceDetails
        updateState(deviceDetails)
    }

    /**
     * Asynchronously saves the SMB server information after validating the input.
     */
    suspend fun save() {
        if (_uiState.value.sanitizeAndValidateInputFields()) {
            serverInfoRepo.upsertSmbServer(_uiState.value.currentUiData.toSmbServerEntity())
        }
    }

    /**
     * Updates the [uiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    private fun updateState(smbServerInfoUiData: SmbServerInfoUiData) {
        _uiState.update { currState ->
            currState.copy(
                currentUiData = smbServerInfoUiData,
                isUiDataValid = currState.sanitizeAndValidateInputFields()
            )
        }
    }
}
