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
import com.ad.backupfiles.ui.shared.EditScreenUiState
import com.ad.backupfiles.ui.shared.SmbServerInfoUiData
import com.ad.backupfiles.ui.shared.sanitizeAndValidateInputFields
import com.ad.backupfiles.ui.shared.toDto
import com.ad.backupfiles.ui.shared.toSmbServerEntity
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
    private val _uiState = MutableStateFlow(EditScreenUiState())
    val uiState: StateFlow<EditScreenUiState> = _uiState.asStateFlow()

    var userInputState by mutableStateOf(SmbServerInfoUiData())
        private set

    init {
        viewModelScope.launch {
            _uiState.value = serverInfoRepo.getSmbServerStream(smbServerId).filterNotNull().first().toUiState(true)
            userInputState = _uiState.value.currentUiData
        }
    }

    /**
     * Update the item in the [ItemsRepository]'s data source
     */
    suspend fun updateItem() {
        if (_uiState.value.sanitizeAndValidateInputFields()) {
            serverInfoRepo.upsertSmbServer(_uiState.value.currentUiData.toSmbServerEntity())
        }
    }

    fun updateUiState(smbServerInfoUiData: SmbServerInfoUiData) {
        userInputState = smbServerInfoUiData
        updateState(smbServerInfoUiData)
    }

    suspend fun canConnectToServer(): Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext if (smb.canConnect(_uiState.value.toDto())) {
                Log.d(TAG, "Successfully connected with new changes")
                true
            } else {
                Log.e(TAG, "Unable to connect with SMB Server ${_uiState.value.toDto()}")
                false
            }
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
