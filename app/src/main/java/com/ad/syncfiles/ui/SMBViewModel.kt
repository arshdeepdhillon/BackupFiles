package com.ad.syncfiles.ui

import androidx.lifecycle.ViewModel
import com.ad.syncfiles.data.SMBUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SMBViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SMBUiState())
    val uiState: StateFlow<SMBUiState> = _uiState.asStateFlow()

    fun setUrl(url: String) {
        _uiState.update { currentState ->
            currentState.copy(serverUrl = url)
        }
    }

    fun setUsername(username: String) {
        _uiState.update { currentState ->
            currentState.copy(username = username)
        }
    }

    fun setPassword(password: String) {
        _uiState.update { currentState ->
            currentState.copy(password = password)
        }
    }

    fun resetSMB() {
        _uiState.value = SMBUiState()
    }
}