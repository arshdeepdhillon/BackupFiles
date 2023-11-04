package com.ad.backupfiles.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ad.backupfiles.data.entity.DirectoryInfo
import com.ad.backupfiles.data.entity.SMBServerWithSavedDirs
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
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(directory: DirectoryInfo): Long

    @Delete
    suspend fun delete(directory: DirectoryInfo)

    @Transaction
    @Query("SELECT * FROM smb_server_info WHERE smbServerId = :smbServerId LIMIT 1") // TODO Use better order clause
    fun getSmbServerWithDirectories(smbServerId: Int): Flow<SMBServerWithSavedDirs>

    @Query("SELECT EXISTS(SELECT * FROM smb_server_info as smbInfo, directory_info as dirInfo WHERE smbInfo.smbServerId = :smbServerId AND dirInfo.dirPath = :dirPath LIMIT 1)")
    fun isDirectorySaved(smbServerId: Int, dirPath: String): Boolean


    @Query("UPDATE directory_info SET lastSynced = :currentTime WHERE smbServerId = :smbId AND dirId = :dirId")
    fun updateSyncTime(dirId: Long, smbId: Int, currentTime: Long)

    @Query("SELECT * FROM directory_info WHERE smbServerId = :smbId AND dirId = :dirId")
    suspend fun getById(dirId: Long, smbId: Int): DirectoryInfo?

}