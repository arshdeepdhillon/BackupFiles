package com.ad.syncfiles.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ad.syncfiles.data.ServerInfo
import com.ad.syncfiles.data.ServerInfoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel to retrieve all the servers in the Room database.
 */
class HomeViewModel(serverInfoRepo: ServerInfoRepository) : ViewModel() {

    /**
     * Holds the state of [HomeUiState]. Items are retrieved from [ServerInfoRepository] and mapped to [HomeUiState]
     */
    val homeUiState: StateFlow<HomeUiState> = serverInfoRepo.getAllSharedServersAscStream().map { HomeUiState(it) }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(
            TIMEOUT_MILLIS
        ), initialValue = HomeUiState()
    )

    companion object {
        // TODO reason for this wait...?
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * Ui state for the Home screen
 */
data class HomeUiState(val sharedServerList: List<ServerInfo> = listOf())