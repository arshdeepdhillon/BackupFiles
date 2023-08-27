package com.ad.syncfiles.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerInfoDao {

    @Upsert
    suspend fun upsert(smbInfo: ServerInfo)

    @Delete
    suspend fun delete(smbInfo: ServerInfo)

    @Query("SELECT * FROM server_info ORDER BY createdDate ASC")
    fun getByAddedDate(): Flow<List<ServerInfo>>

    @Query("SELECT * FROM server_info WHERE id = :id LIMIT 1")
    fun getById(id: Int): Flow<ServerInfo>
}