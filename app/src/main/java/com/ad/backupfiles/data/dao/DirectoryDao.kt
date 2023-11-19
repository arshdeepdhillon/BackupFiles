package com.ad.backupfiles.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ad.backupfiles.data.entity.DirectoryInfo
import com.ad.backupfiles.data.entity.DirectorySyncInfo
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
interface DirectoryDao {
    @Transaction
    suspend fun insertAndQueueForBackup(directory: DirectoryInfo): Long? {
        val insertedId = insert(directory)
        insertDirectoryForSync(DirectorySyncInfo(dirId = insertedId, smbServerId = directory.smbServerId, dirPath = directory.dirPath))
        return insertedId
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDirectoryForSync(dirToQueue: DirectorySyncInfo): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(directory: DirectoryInfo): Long


    @Delete
    suspend fun delete(directory: DirectoryInfo)

    @Transaction
    @Query("SELECT * FROM smb_server_info WHERE smbServerId = :smbServerId")
    fun getSmbServerWithDirectories(smbServerId: Long): Flow<SMBServerWithSavedDirs>

    @Query("SELECT EXISTS(SELECT * FROM smb_server_info as smbInfo, directory_info as dirInfo WHERE smbInfo.smbServerId = :smbServerId AND dirInfo.dirPath = :dirPath LIMIT 1)")
    fun isDirectorySaved(smbServerId: Long, dirPath: String): Boolean


    @Query("UPDATE directory_info SET lastSynced = :currentTime WHERE smbServerId = :smbId AND dirId = :dirId")
    suspend fun updateSyncTime(dirId: Long, smbId: Long, currentTime: Long)

    @Query("SELECT * FROM directory_info WHERE smbServerId = :smbServerId AND dirId = :dirId")
    suspend fun getDirectoryById(dirId: Long, smbServerId: Long): DirectoryInfo?

    @Query("SELECT * FROM directory_sync_pending WHERE smbServerId = :smbServerId")
    fun getPendingSyncDirectories(smbServerId: Long): List<DirectorySyncInfo>

    @Delete
    suspend fun deleteFromSync(synDir: DirectorySyncInfo)

    @Query("SELECT * FROM directory_sync_pending WHERE itemId = :syncId LIMIT 1")
    suspend fun getPendingSyncDirectoryById(syncId: Long): DirectorySyncInfo?

    @Query("DELETE FROM directory_sync_pending WHERE smbServerId = :smbServerId")
    suspend fun deleteAllFromSync(smbServerId: Long)
}