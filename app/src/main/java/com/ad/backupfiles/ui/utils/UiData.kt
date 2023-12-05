package com.ad.backupfiles.ui.utils

import com.ad.backupfiles.data.entity.SmbServerDto
import com.ad.backupfiles.data.entity.SmbServerInfo

/*
 * @author : Arshdeep Dhillon
 * @created : 29-Oct-23
*/

/** Wait 5 seconds before stopping the upstream flows */
const val DELAY_UPSTREAM_TIMEOUT_MILLIS = 5_000L

/**
 * Represents Ui State of [SmbServerData].
 */
data class SMBServerUiState(
    val currentUiData: SmbServerData = SmbServerData(),
    val isUiDataValid: Boolean = false,
)

fun SMBServerUiState.toUiData(): SmbServerDto {
    this.currentUiData.let {
        return SmbServerDto(
            username = it.username,
            password = it.password,
            serverAddress = it.serverAddress,
            sharedFolder = it.sharedFolderName,
        )
    }
}

data class SmbServerData(
    val id: Long = 0,
    val serverAddress: String = "",
    val username: String = "",
    val password: String = "",
    val sharedFolderName: String = "",
)

/**
 * Extension function to convert [SMBServerUiState] to [SmbServerInfo].
 */
fun SmbServerData.toSmbServerEntity(): SmbServerInfo = SmbServerInfo(
    smbServerId = id,
    serverAddress = serverAddress,
    username = username,
    password = password,
    sharedFolderName = sharedFolderName,
)

/**
 * Extension function to convert [SmbServerInfo] to [SmbServerData].
 */
fun SmbServerInfo.toUiData(): SmbServerData = SmbServerData(
    id = smbServerId,
    serverAddress = serverAddress,
    username = username,
    password = password,
    sharedFolderName = sharedFolderName,
)

/**
 * Sanitizes and validates the input fields in the ServerInfoUiState to ensure data integrity.
 *
 * @return `true` if the input fields are valid; `false` otherwise.
 */
fun SMBServerUiState.sanitizeAndValidateInputFields(): Boolean {
    this.currentUiData.also {
        // TODO this doesn't work!!
        it.username.trim()
        it.serverAddress.trim()
        it.sharedFolderName.trim()
    }.also {
        return it.serverAddress.isNotBlank()
    }
}
