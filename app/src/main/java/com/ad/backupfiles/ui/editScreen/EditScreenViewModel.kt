package com.ad.backupfiles.ui.editScreen

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ad.backupfiles.data.entity.toUiState
import com.ad.backupfiles.data.repository.api.SmbServerInfoApi
import com.ad.backupfiles.smb.api.SMBClientApi
import com.ad.backupfiles.ui.utils.SMBServerUiState
import com.ad.backupfiles.ui.utils.SmbServerData
import com.ad.backupfiles.ui.utils.sanitizeData
import com.ad.backupfiles.ui.utils.toUiData
import com.ad.backupfiles.ui.utils.validateData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Updates the fields of an item from [SmbServerInfoApi]'s data source
 */
class EditScreenViewModel(
    @Suppress("unused") private val stateHandle: SavedStateHandle,
    private val smbServerApi: SmbServerInfoApi,
    private val smbClientApi: SMBClientApi,
) : ViewModel() {
    private val TAG = EditScreenViewModel::class.java.simpleName
    private val smbServerId: Long = checkNotNull(stateHandle[EditScreenDestination.argKey])

    /**
     * Holds current UI state
     */
    private val _viewState = MutableStateFlow(SMBServerUiState())
    val viewState: StateFlow<SMBServerUiState> = _viewState.asStateFlow()

    var userInputState by mutableStateOf(SmbServerData())
        private set

    init {
        viewModelScope.launch {
            _viewState.value = smbServerApi
                .getSmbServerStream(smbServerId)
                .filterNotNull()
                .first()
                .let { it.toUiState(validateData(it.toUiData())) }

            userInputState = _viewState.value.currentUiData
        }
    }

    /**
     * Saves the SMB related changes into database.
     */
    suspend fun saveChanges() {
        if (validateData(_viewState.value.currentUiData)) {
            smbServerApi.upsertSmbServer(_viewState.value.currentUiData)
        }
    }

    /**
     * Updates the UI state based on the provided [smbServerData] data.
     *
     * @param smbServerData The SMB server data containing information to update the UI state.
     */
    fun updateUiState(smbServerData: SmbServerData) {
        userInputState = smbServerData

        _viewState.update { currState ->
            val sanitizedUiData = sanitizeData(smbServerData)
            currState.copy(currentUiData = sanitizedUiData, isValid = validateData(sanitizedUiData))
        }
    }

    /**
     * Attempts to establish a connection with the SMB server to check if it's reachable,
     * using the data from the current view state.
     *
     * @return `true` if the connection is successful, `false` otherwise.
     */
    suspend fun canConnectToServer(): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext if (smbClientApi.canConnect(_viewState.value.toUiData())) {
                Log.d(TAG, "Successfully connected with new changes")
                true
            } else {
                Log.d(TAG, "Unable to connect with SMB Server ${_viewState.value.toUiData()}")
                false
            }
        }
    }
}
