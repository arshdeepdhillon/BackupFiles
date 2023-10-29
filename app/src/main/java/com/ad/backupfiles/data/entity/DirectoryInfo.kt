package com.ad.backupfiles.data.entity

import android.net.Uri
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

@Entity(
    tableName = "directory_info",
    foreignKeys = [ForeignKey(
        entity = SmbServerInfo::class,
        parentColumns = arrayOf("smbServerId"),
        childColumns = arrayOf("smbServerId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class DirectoryInfo(
    @PrimaryKey(autoGenerate = true)
    val dirId: Int = 0,
    val smbServerId: Int,
    val dirPath: String,
)

data class SMBServerWithSavedDirs(
    @Embedded
    val smbServer: SmbServerInfo,
    @Relation(
        parentColumn = "smbServerId",
        entityColumn = "smbServerId"
    )
    val savedDirs: List<DirectoryInfo>,
)

/**
 * Represents a saved directory item.
 *
 * @param dirId The unique identifier for the directory.
 * @param dirUri The URI of the directory.
 * @param dirName The name of the directory.
 * @param lastModified The timestamp of the last modification.
 * @param itemCount The number of items in the directory.
 * @param isDirectory Indicates whether the item is a directory.
 */
data class DirectoryDto(val dirId: Int, val dirUri: Uri, val dirName: String, val lastModified: Long, val itemCount: Int, val isDirectory: Boolean)

/**
 * Converts a DirectoryInfo to a DirectoryDto object.
 */
fun DirectoryInfo.toDto(dirUri: Uri, dirName: String, lastModified: Long, itemCount: Int, isDirectory: Boolean): DirectoryDto = DirectoryDto(
    dirId = this.dirId, dirUri = dirUri, dirName = dirName, lastModified = lastModified, itemCount = itemCount, isDirectory = isDirectory
)
