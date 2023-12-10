package com.ad.backupfiles.ui.smbServer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ad.backupfiles.di.api.ApplicationModuleApi
import com.ad.backupfiles.ui.utils.SmbServerData
import com.ad.backupfiles.ui.utils.toSmbServerEntity
import com.ad.backupfiles.ui.utils.toUiData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

class DetailScreenViewModel(
    @Suppress("unused") private val stateHandle: SavedStateHandle,
    private val appModule: ApplicationModuleApi,
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val smbServerId: Long = checkNotNull(stateHandle[DetailScreenDestination.argKey])

    /**
     * Holds current UI state
     */
    var uiState: StateFlow<DetailScreenUiState> =
        appModule.smbServerApi.getSmbServerStream(smbServerId).filterNotNull().map {
            DetailScreenUiState(deviceDetails = it.toUiData())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = DetailScreenUiState(),
        )

    suspend fun deleteSmbServer() {
        appModule.smbServerApi.deleteSmbServer(uiState.value.deviceDetails.toSmbServerEntity())
    }
}

/**
 * Represents the latest UI state for displaying details of an SMB server.
 *
 * @property deviceDetails Details of the SMB server.
 */
data class DetailScreenUiState(val deviceDetails: SmbServerData = SmbServerData())
