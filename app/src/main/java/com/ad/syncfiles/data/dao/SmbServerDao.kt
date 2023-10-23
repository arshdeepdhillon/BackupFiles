package com.ad.syncfiles.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.ad.syncfiles.data.entity.SmbServerInfo
import kotlinx.coroutines.flow.Flow

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * DAO of [SmbServerInfo] database
 */
@Dao
interface SmbServerDao {

    @Upsert
    suspend fun upsert(smbInfo: SmbServerInfo)

    @Delete
    suspend fun delete(smbInfo: SmbServerInfo)

    @Query("SELECT * FROM smb_server_info ORDER BY createdDate ASC")
    fun getByAddedDate(): Flow<List<SmbServerInfo>>

    @Query("SELECT * FROM smb_server_info WHERE smbServerId = :id LIMIT 1")
    fun getByIdStream(id: Int): Flow<SmbServerInfo>

    @Query("SELECT * FROM smb_server_info WHERE smbServerId = :id LIMIT 1")
    fun getById(id: Int): SmbServerInfo
}