package com.ad.syncfiles.data

import androidx.compose.runtime.saveable.listSaver

data class SMBUiState(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SMBUiState) return false
        if (serverUrl != other.serverUrl) return false
        if (username != other.username) return false
        if (password != other.password) return false
        return true
    }

    override fun hashCode(): Int {
        var result = serverUrl.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + password.hashCode()
        return result
    }
}

//val SMBSaver = Saver(
//    save = { saveable: SMBUiState -> SMBUiState(serverUrl = saveable.serverUrl, username = saveable.username, password = saveable.password) },
//    restore = { orig: SMBUiState -> SMBUiState(serverUrl = orig.serverUrl, username = orig.username, password = orig.password) }
//)

/**
 * Custom Saveable used for saving/restoring state on configuration changes
 */
val SMBSaver = listSaver<SMBUiState, Any>(
    save = { listOf(it.serverUrl, it.username, it.password) },
    restore = { SMBUiState(serverUrl = it[0] as String, username = it[1] as String, password = it[2] as String) }
)