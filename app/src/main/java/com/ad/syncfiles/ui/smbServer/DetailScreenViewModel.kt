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

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

class DetailScreenViewModel(
    stateHandle: SavedStateHandle,
    private val serverInfoRepo: SmbServerInfoRepository,
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val smbServerId: Int = checkNotNull(stateHandle[DetailScreenDestination.argKey])

    /**
     * Holds current UI state
     */
    var uiState: StateFlow<DetailUIState> =
        serverInfoRepo.getSmbServerStream(smbServerId).filterNotNull().map {
            DetailUIState(deviceDetails = it.toDetails())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = DetailUIState()
        )


    suspend fun deleteSmbServer() {
        serverInfoRepo.deleteSmbServer(uiState.value.deviceDetails.toSmbServerInfo())
    }


}

/**
 * Represents Ui State of [ServerDetails].
 */
data class DetailUIState(val deviceDetails: ServerDetails = ServerDetails())