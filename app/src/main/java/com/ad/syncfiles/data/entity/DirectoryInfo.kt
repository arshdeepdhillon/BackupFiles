package com.ad.syncfiles.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation


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