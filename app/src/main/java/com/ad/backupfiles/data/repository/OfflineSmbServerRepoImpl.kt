package com.ad.backupfiles.data.repository

import com.ad.backupfiles.data.dao.SmbServerDao
import com.ad.backupfiles.data.entity.SmbServerInfo
import com.ad.backupfiles.data.repository.api.SmbServerInfoApi
import com.ad.backupfiles.ui.utils.SmbServerData
import com.ad.backupfiles.ui.utils.toSmbServerEntity
import kotlinx.coroutines.flow.Flow

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

class OfflineSmbServerRepoImpl(private val smbServerDao: SmbServerDao) : SmbServerInfoApi {

    /**
     * @see SmbServerInfoApi.getAllSmbServersAscStream
     */
    override fun getAllSmbServersAscStream(): Flow<List<SmbServerInfo>> =
        smbServerDao.getByAddedDate()

    /**
     * @see SmbServerInfoApi.getSmbServerStream
     */
    override fun getSmbServerStream(smbServerId: Long): Flow<SmbServerInfo?> =
        smbServerDao.getByIdStream(smbServerId)

    /**
     * @see SmbServerInfoApi.getSmbServer
     */
    override suspend fun getSmbServer(id: Long): SmbServerInfo = smbServerDao.getById(id)

    /**
     * @see SmbServerInfoApi.upsertSmbServer
     */
    override suspend fun upsertSmbServer(smbServerData: SmbServerData) =
        smbServerDao.upsert(smbServerData.toSmbServerEntity())

    /**
     * @see SmbServerInfoApi.deleteSmbServer
     */
    override suspend fun deleteSmbServer(smbServerInfo: SmbServerInfo) =
        smbServerDao.delete(smbServerInfo)
}
