package com.ad.syncfiles.data.repository

import com.ad.syncfiles.data.entity.SmbServerInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides data access to [SmbServerInfo].
 */
interface SmbServerInfoRepository {

    /**
     * Retrieves a flow of lists containing SMB server information in ascending order.
     * @return A [Flow] emitting lists of [SmbServerInfo] objects.
     */
    fun getAllSmbServersAscStream(): Flow<List<SmbServerInfo>>

    /**
     * Retrieves a flow of an [SmbServerInfo] object with the specified [id].
     * If the specified [id] does not match any server, it emits `null`.
     *
     * @param id The unique identifier of the SMB server to retrieve.
     * @return A [Flow] emitting [SmbServerInfo] objects or `null` if not found.
     */
    fun getSmbServerStream(id: Int): Flow<SmbServerInfo?>

    /**
     * Inserts or updates an [SmbServerInfo] object in the data source.
     * If the specified [smbServerInfo] already exists, it will be updated; otherwise, a new server entry will be inserted.
     *
     * @param smbServerInfo The [SmbServerInfo] object to insert or update.
     */
    suspend fun upsertSmbServer(smbServerInfo: SmbServerInfo)

    /**
     * Deletes a [SmbServerInfo] specified by [smbServerInfo].
     *
     * @param smbServerInfo The [SmbServerInfo] object to be deleted.
     */
    suspend fun deleteSmbServer(smbServerInfo: SmbServerInfo)

}
