package com.ad.syncfiles.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.ad.syncfiles.data.entity.DirectoryInfo
import com.ad.syncfiles.data.entity.SMBServerWithSavedDirs
import kotlinx.coroutines.flow.Flow

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Data access of [DirectoryInfo]
 */
@Dao
interface SavedDirectoryDao {
    @Upsert
    suspend fun upsert(directory: DirectoryInfo)

    @Delete
    suspend fun delete(directory: DirectoryInfo)

    @Transaction
    @Query("SELECT * FROM smb_server_info WHERE smbServerId = :smbServerId LIMIT 1") // TODO Use better order clause
    fun getSmbServerWithDirectories(smbServerId: Int): Flow<SMBServerWithSavedDirs>

    @Query("SELECT EXISTS(SELECT * FROM smb_server_info as smbInfo, directory_info as dirInfo WHERE smbInfo.smbServerId = :smbServerId and dirInfo.dirPath = :dirPath LIMIT 1)")
    fun isDirectorySaved(smbServerId: Int, dirPath: String): Boolean

}