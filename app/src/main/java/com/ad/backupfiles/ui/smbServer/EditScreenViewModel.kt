package com.ad.backupfiles.ui.smbServer

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ad.backupfiles.data.entity.toUiState
import com.ad.backupfiles.data.repository.api.SmbServerInfoApi
import com.ad.backupfiles.di.api.ApplicationModuleApi
import com.ad.backupfiles.smb.SMBClientImpl
import com.ad.backupfiles.ui.utils.SMBServerUiState
import com.ad.backupfiles.ui.utils.SmbServerData
import com.ad.backupfiles.ui.utils.sanitizeAndValidateInputFields
import com.ad.backupfiles.ui.utils.toSmbServerEntity
import com.ad.backupfiles.ui.utils.toUiData
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
    private val appModule: ApplicationModuleApi,
) : ViewModel() {
    private val TAG = EditScreenViewModel::class.java.simpleName
    private val smb = SMBClientImpl()
    private val smbServerId: Long = checkNotNull(stateHandle[EditScreenDestination.argKey])

    /**
     * Holds current UI state
     */
    private val _uiState = MutableStateFlow(SMBServerUiState())
    val uiState: StateFlow<SMBServerUiState> = _uiState.asStateFlow()

    var userInputState by mutableStateOf(SmbServerData())
        private set

    init {
        viewModelScope.launch {
            _uiState.value = appModule.smbServerApi.getSmbServerStream(smbServerId).filterNotNull().first().toUiState(true)
            userInputState = _uiState.value.currentUiData
        }
    }

    /**
     * Update the item in the [ItemsRepository]'s data source
     */
    suspend fun updateItem() {
        if (_uiState.value.sanitizeAndValidateInputFields()) {
            appModule.smbServerApi.upsertSmbServer(_uiState.value.currentUiData.toSmbServerEntity())
        }
    }

    fun updateUiState(smbServerData: SmbServerData) {
        userInputState = smbServerData
        updateState(smbServerData)
    }

    suspend fun canConnectToServer(): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext if (smb.canConnect(_uiState.value.toUiData())) {
                Log.d(TAG, "Successfully connected with new changes")
                true
            } else {
                Log.e(TAG, "Unable to connect with SMB Server ${_uiState.value.toUiData()}")
                false
            }
        }
    }

    /**
     * Updates the [uiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    private fun updateState(smbServerData: SmbServerData) {
        _uiState.update { currState ->
            currState.copy(
                currentUiData = smbServerData,
                isUiDataValid = currState.sanitizeAndValidateInputFields(),
            )
        }
    }
}
