package com.ad.syncfiles.data.repository

import com.ad.syncfiles.data.entity.SmbServerInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and retrieve of [SmbServerInfo] from a given data source.
 */
interface SmbServerInfoRepository {
    /**
     * Retrieve all the items from the the given data source.
     */
    fun getAllSmbServersAscStream(): Flow<List<SmbServerInfo>>

    /**
     * Retrieve an serverInfo from the given data source that matches with the [id].
     */
    fun getSmbServerStream(id: Int): Flow<SmbServerInfo?>

    /**
     * Upsert serverInfo in the data source
     */
    suspend fun upsertSmbServer(smbServerInfo: SmbServerInfo)

    /**
     * Delete serverInfo from the data source
     */
    suspend fun deleteSmbServer(smbServerInfo: SmbServerInfo)

}
