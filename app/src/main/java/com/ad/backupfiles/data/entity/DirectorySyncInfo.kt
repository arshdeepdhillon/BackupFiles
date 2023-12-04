package com.ad.backupfiles.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * @author : Arshdeep Dhillon
 * @created : 04-Nov-23
*/

/**
 * Represents information about a directory to be synchronized with the SMB server.
 *
 * @property itemId The unique identifier for this directory sync item.
 * @property dirId The identifier for the directory.
 * @property smbServerId The identifier of the SMB server associated with this directory.
 * @property dirPath The URI referencing the directory on user's device.
 */
@Entity(
    tableName = "directory_sync_pending",
    foreignKeys = [
        ForeignKey(
            entity = SmbServerInfo::class,
            parentColumns = arrayOf("smbServerId"),
            childColumns = arrayOf("smbServerId"),
            onDelete = ForeignKey.CASCADE,
        ), ForeignKey(
            entity = DirectoryInfo::class,
            parentColumns = arrayOf("dirId"),
            childColumns = arrayOf("dirId"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(
            value = ["dirId", "smbServerId", "dirPath"],
            unique = true,
        ),
    ],
)
data class DirectorySyncInfo(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0,
    val dirId: Long,
    val smbServerId: Long,
    val dirPath: String,
)
