package com.ad.backupfiles.ui.smbServer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ad.backupfiles.data.repository.SmbServerInfoRepo
import com.ad.backupfiles.ui.utils.SmbServerInfoUiData
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
    stateHandle: SavedStateHandle,
    private val serverInfoRepo: SmbServerInfoRepo,
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val smbServerId: Long = checkNotNull(stateHandle[DetailScreenDestination.argKey])

    /**
     * Holds current UI state
     */
    var uiState: StateFlow<DetailUIState> =
        serverInfoRepo.getSmbServerStream(smbServerId).filterNotNull().map {
            DetailUIState(deviceDetails = it.toUiData())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = DetailUIState()
        )


    suspend fun deleteSmbServer() {
        serverInfoRepo.deleteSmbServer(uiState.value.deviceDetails.toSmbServerEntity())
    }


}

/**
 * Represents Ui State of [SmbServerInfoUiData].
 */
data class DetailUIState(val deviceDetails: SmbServerInfoUiData = SmbServerInfoUiData())