package com.ad.backupfiles.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ad.backupfiles.data.entity.SmbServerInfo
import com.ad.backupfiles.data.repository.api.SmbServerInfoApi
import com.ad.backupfiles.di.api.ApplicationModuleApi
import com.ad.backupfiles.ui.utils.DELAY_UPSTREAM_TIMEOUT_MILLIS
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * ViewModel for the [HomeScreen]
 */
class HomeViewModel(@Suppress("unused") private val appModule: ApplicationModuleApi) : ViewModel() {

    /**
     * Holds the state of [HomeUiState]. Items are retrieved from [SmbServerInfoApi] and mapped to [HomeUiState]
     */
    val homeUiState: StateFlow<HomeUiState> =
        appModule.smbServerApi.getAllSmbServersAscStream().map { HomeUiState(it) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(DELAY_UPSTREAM_TIMEOUT_MILLIS),
            initialValue = HomeUiState(),
        )
}

/**
 * Ui state for the Home screen
 */
data class HomeUiState(val sharedServers: List<SmbServerInfo> = listOf())
