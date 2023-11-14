package com.ad.backupfiles.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Represents information about a directory stored in this device.
 *
 * @param dirId The unique identifier for the directory. It's auto-generated.
 * @param smbServerId The identifier of the SMB server associated with the directory.
 * @param dirPath The URI referencing the directory on user's device.
 * @param lastSynced The timestamp when the directory was last synchronized, or null if not yet synchronized.
 */
@Entity(
    tableName = "directory_info",
    foreignKeys = [ForeignKey(
        entity = SmbServerInfo::class,
        parentColumns = arrayOf("smbServerId"),
        childColumns = arrayOf("smbServerId"),
        onDelete = ForeignKey.CASCADE
    )], indices = [Index(
        value = ["smbServerId", "dirPath"], unique = true
    )]
)
data class DirectoryInfo(
    @PrimaryKey(autoGenerate = true)
    val dirId: Long = 0,
    val smbServerId: Long,
    val dirPath: String, // URI path to the folder
    val dirName: String?,
    val lastSynced: Long? = null,
)

data class SMBServerWithSavedDirs(
    @Embedded
    val smbServer: SmbServerInfo,
    @Relation(parentColumn = "smbServerId", entityColumn = "smbServerId")
    val savedDirs: List<DirectoryInfo>,
)

/**
 * Data transfer object representing a saved directory.
 *
 * @property dirPath The path of the directory.
 * @property dirName The name of the directory or null.
 * @property smbServerId The identifier of the associated SMB server.
 * @property lastSynced The [java.time.Instant.now] seconds since the directory was last synchronized (can be null if it has not yet been synced).
 */
data class DirectoryDto(val dirId: Long, val dirPath: String, val dirName: String?, val smbServerId: Long, val lastSynced: Long?)


/**
 * Converts from DirectoryInfo to DirectoryDto object.
 */
fun DirectoryInfo.toDto(): DirectoryDto = DirectoryDto(
    dirId = this.dirId, dirPath = this.dirPath, dirName = this.dirName, lastSynced = this.lastSynced, smbServerId = this.smbServerId
)