package com.ad.syncfiles.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ad.syncfiles.ui.smbServer.ServerInfoUiState
import com.ad.syncfiles.ui.smbServer.toDetails

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

@Entity(tableName = "smb_server_info")
data class SmbServerInfo(
    @PrimaryKey(autoGenerate = true)
    val smbServerId: Int = 0,
    val serverAddress: String,
    val username: String,
    val password: String,
    val sharedFolderName: String,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    val createdDate: Long = System.currentTimeMillis(),
)

data class SmbServerDto(val username: String, val password: String, val serverAddress: String, val sharedFolder: String)

fun SmbServerInfo.toDto(): SmbServerDto = SmbServerDto(
    serverAddress = this.serverAddress,
    username = this.username,
    password = this.password,
    sharedFolder = this.sharedFolderName
)

fun SmbServerInfo.toUiState(isEntryValid: Boolean): ServerInfoUiState = ServerInfoUiState(
    serverDetails = this.toDetails(),
    isValid = isEntryValid
)