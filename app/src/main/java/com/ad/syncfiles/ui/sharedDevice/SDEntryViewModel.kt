package com.ad.syncfiles.ui.sharedDevice

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ad.syncfiles.data.ServerInfo
import com.ad.syncfiles.data.ServerInfoRepository

class SDEntryViewModel(private val serverInfoRepo: ServerInfoRepository) : ViewModel() {
    /**
     * Holds current UI state
     */
    var uiState by mutableStateOf(UIState())
        private set


    fun updateUiState(deviceDetails: SharedDeviceDetails) {
        uiState = UIState(deviceDetails = deviceDetails, isEntryValid = validateInput(deviceDetails))
    }

    private fun validateInput(deviceDetails: SharedDeviceDetails = uiState.deviceDetails): Boolean {
        return with(deviceDetails) {
            serverUrl.isNotBlank()
        }
    }

    suspend fun save() {
        if (validateInput()) {
            serverInfoRepo.upsertServerInfo(uiState.deviceDetails.toServerInfo())
        }
    }


//    fun getSharedFiles() {
//        val smbClient = SMB()
//        smbClient.listFiles()
////        viewModelScope.launch {
//////            SMB().listFiles()
////            val result = withContext(Dispatchers.IO){
////                delay(2000)
////                true
////            }
////            Log.d("getSharedFiles", "$result")
////        }
//    }

}

/**
 * Represents Ui State of [SharedDeviceDetails].
 */
data class UIState(
    val deviceDetails: SharedDeviceDetails = SharedDeviceDetails(),
    val isEntryValid: Boolean = false,
)

data class SharedDeviceDetails(
    val id: Int = 0,
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val sharedFolderName: String = "",
)

/**
 * Extension function to convert [UIState] to [ServerInfo].
 */
fun SharedDeviceDetails.toServerInfo(): ServerInfo = ServerInfo(
    id = id,
    serverUrl = serverUrl,
    username = username,
    password = password,
    sharedFolderName = sharedFolderName
)