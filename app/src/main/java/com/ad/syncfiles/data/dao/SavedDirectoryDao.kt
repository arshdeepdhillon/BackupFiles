package com.ad.syncfiles.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.ad.syncfiles.data.entity.DirectoryInfo
import com.ad.syncfiles.data.entity.SMBServerWithSavedDirs
import kotlinx.coroutines.flow.Flow


/**
 * Data access of [DirectoryInfo]
 */
@Dao
interface SavedDirectoryDao {
    @Upsert
    suspend fun upsert(directory: DirectoryInfo)

    @Delete
    suspend fun delete(directory: DirectoryInfo)

//    @Query("SELECT * FROM directory WHERE id = :savedDirId LIMIT 1")
//    fun getById(savedDirId: Int): Flow<SavedDirectory>

    @Transaction
    @Query("SELECT * FROM smb_server_info WHERE smbServerId = :smbServerId LIMIT 1") // TODO Use better order clause
    fun getSmbServerWithDirectories(smbServerId: Int): Flow<SMBServerWithSavedDirs>
}