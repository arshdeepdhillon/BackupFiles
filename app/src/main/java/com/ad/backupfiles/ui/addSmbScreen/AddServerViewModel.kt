package com.ad.backupfiles.ui.addSmbScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ad.backupfiles.data.repository.api.SmbServerInfoApi
import com.ad.backupfiles.ui.utils.SMBServerUiState
import com.ad.backupfiles.ui.utils.SmbServerData
import com.ad.backupfiles.ui.utils.sanitizeData
import com.ad.backupfiles.ui.utils.validateData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

class AddServerViewModel(private val smbServerApi: SmbServerInfoApi) : ViewModel() {
    /**
     * Holds current UI state
     */
    private val _viewState = MutableStateFlow(SMBServerUiState())
    val viewState: StateFlow<SMBServerUiState> = _viewState.asStateFlow()

    var userInputState by mutableStateOf(SmbServerData())
        private set

    /**
     * Syncs the UI state with the given [deviceDetails] and determines whether the input is valid.
     * Typically used to update the internal state in response to changes in [AddScreen].
     *
     * @param deviceDetails The server details to update the UI state with.
     */
    fun updateUiState(deviceDetails: SmbServerData) {
        userInputState = deviceDetails
        _viewState.update { currState ->
            val sanitizedUiData = sanitizeData(deviceDetails)
            currState.copy(currentUiData = sanitizedUiData, isValid = validateData(sanitizedUiData))
        }
    }

    /**
     * Asynchronously saves the SMB server information after validating the input.
     */
    suspend fun save() {
        if (validateData(_viewState.value.currentUiData)) {
            smbServerApi.upsertSmbServer(_viewState.value.currentUiData)
        }
    }
}
