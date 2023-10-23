package com.ad.syncfiles.ui.smbServer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ad.syncfiles.data.entity.SmbServerDto
import com.ad.syncfiles.data.entity.SmbServerInfo
import com.ad.syncfiles.data.repository.SmbServerInfoRepository
import com.ad.syncfiles.smb.SMBClientWrapper

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

class AddScreenViewModel(private val serverInfoRepo: SmbServerInfoRepository) : ViewModel() {
    private val TAG = AddScreenViewModel::class.java.simpleName

    private val smb = SMBClientWrapper()

    /**
     * Holds current UI state
     */
    var uiState by mutableStateOf(ServerInfoUiState())
        private set


    /**
     * Syncs the UI state with the given [deviceDetails] and determines whether the input is valid.
     * Typically used to update the internal state in response to changes in [AddScreen].
     *
     * @param deviceDetails The server details to update the UI state with.
     */
    fun handleUiStateChange(deviceDetails: ServerDetails) {
        uiState = ServerInfoUiState(serverDetails = deviceDetails, isValid = validateInput(deviceDetails))
    }

    private fun validateInput(deviceDetails: ServerDetails = uiState.serverDetails): Boolean {
        return with(deviceDetails) {
            serverAddress.isNotBlank()
        }
    }

    /**
     * Asynchronously saves the SMB server information after validating the input.
     */
    suspend fun save() {
        if (validateInput()) {
            serverInfoRepo.upsertSmbServer(uiState.serverDetails.toSmbServerInfo())
        }
    }
}

/**
 * Represents Ui State of [ServerDetails].
 */
data class ServerInfoUiState(
    val serverDetails: ServerDetails = ServerDetails(),
    val isValid: Boolean = false,
)

fun ServerInfoUiState.toDto(): SmbServerDto {
    this.serverDetails.let {
        return SmbServerDto(username = it.username, password = it.password, serverAddress = it.serverAddress, sharedFolder = it.sharedFolderName)
    }
}

data class ServerDetails(
    val id: Int = 0,
    val serverAddress: String = "",
    val username: String = "",
    val password: String = "",
    val sharedFolderName: String = "",
)

/**
 * Extension function to convert [ServerInfoUiState] to [SmbServerInfo].
 */
fun ServerDetails.toSmbServerInfo(): SmbServerInfo = SmbServerInfo(
    smbServerId = id,
    serverAddress = serverAddress,
    username = username,
    password = password,
    sharedFolderName = sharedFolderName
)

/**
 * Extension function to convert [ServerInfoUiState] to [SmbServerInfo].
 */
fun SmbServerInfo.toDetails(): ServerDetails = ServerDetails(
    id = smbServerId,
    serverAddress = serverAddress,
    username = username,
    password = password,
    sharedFolderName = sharedFolderName
)
