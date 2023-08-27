package com.ad.syncfiles.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and retrieve of [ServerInfo] from a given data source.
 */
interface ServerInfoRepository {
    /**
     * Retrieve all the items from the the given data source.
     */
    fun getAllSharedServersAscStream(): Flow<List<ServerInfo>>

    /**
     * Retrieve an serverInfo from the given data source that matches with the [id].
     */
    fun getServerInfoStream(id: Int): Flow<ServerInfo?>

    /**
     * Upsert serverInfo in the data source
     */
    suspend fun upsertServerInfo(serverInfo: ServerInfo)

    /**
     * Delete serverInfo from the data source
     */
    suspend fun deleteServerInfo(serverInfo: ServerInfo)

}
