package com.ad.backupfiles.data.repository.api

import com.ad.backupfiles.data.entity.SmbServerInfo
import com.ad.backupfiles.ui.utils.SmbServerData
import kotlinx.coroutines.flow.Flow

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Repository that provides data access to [SmbServerInfo].
 */
interface SmbServerInfoApi {

    /**
     * Retrieves a flow of lists containing SMB server information in ascending order.
     * @return A [Flow] emitting lists of [SmbServerInfo] objects.
     */
    fun getAllSmbServersAscStream(): Flow<List<SmbServerInfo>>

    /**
     * Retrieves a flow of an [SmbServerInfo] object with the specified [smbServerId].
     * If the specified [smbServerId] does not match any server, it emits `null`.
     *
     * @param smbServerId The unique identifier of the SMB server to retrieve.
     * @return A [Flow] emitting [SmbServerInfo] objects or `null` if not found.
     */
    fun getSmbServerStream(smbServerId: Long): Flow<SmbServerInfo?>

    /**
     * Updates the given [SmbServerData] if it already exists, otherwise it is inserted.
     *
     * @param smbServerData The [SmbServerData] to update or insert.
     */
    suspend fun upsertSmbServer(smbServerData: SmbServerData)

    /**
     * Deletes a [SmbServerInfo] specified by [smbServerInfo].
     *
     * @param smbServerInfo The [SmbServerInfo] object to be deleted.
     */
    suspend fun deleteSmbServer(smbServerInfo: SmbServerInfo)

    /**
     * Retrieves a an [SmbServerInfo] object with the specified [id].
     * @param id The unique identifier of the SMB server to retrieve.
     * @return A [SmbServerInfo] object.
     */
    suspend fun getSmbServer(id: Long): SmbServerInfo
}
