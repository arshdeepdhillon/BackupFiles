package com.ad.backupfiles.ui.detailServerScreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ad.backupfiles.data.repository.api.SmbServerInfoApi
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

class DetailServerViewModel(
    @Suppress("unused") private val stateHandle: SavedStateHandle,
    private val smbServerApi: SmbServerInfoApi,
) : ViewModel() {
    private val smbServerId: Long = checkNotNull(stateHandle[DetailScreenDestination.argKey])

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    /**
     * Holds current UI state
     */
    var viewState: StateFlow<DetailScreenUiState> =
        smbServerApi.getSmbServerStream(smbServerId).filterNotNull().map {
            DetailScreenUiState(serverInfo = it.toUiData())
        }
//            .onCompletion { println("SHARED FLOW COMPLETED") }
//            .onEach {  println("onEach: $it") }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = DetailScreenUiState(),
            )

    suspend fun deleteSmbServer() {
        smbServerApi.deleteSmbServer(viewState.value.serverInfo.toSmbServerEntity())
    }
}

/**
 * Represents the latest UI state for displaying details of an SMB server.
 *
 * @property serverInfo Details of the SMB server.
 */
data class DetailScreenUiState(val serverInfo: SmbServerData = SmbServerData())
