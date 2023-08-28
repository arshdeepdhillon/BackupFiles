package com.ad.syncfiles.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "smb_server_info")

data class SmbServerInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val serverUrl: String,
    val username: String,
    val password: String,
    val sharedFolderName: String = "shared-folder",
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    val createdDate: Long = System.currentTimeMillis(),
)
