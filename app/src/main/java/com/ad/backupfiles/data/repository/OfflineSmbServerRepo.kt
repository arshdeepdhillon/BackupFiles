package com.ad.backupfiles.data.repository

import com.ad.backupfiles.data.dao.SmbServerDao
import com.ad.backupfiles.data.entity.SmbServerInfo
import kotlinx.coroutines.flow.Flow

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

class OfflineSmbServerRepo(private val smbServerDao: SmbServerDao) : SmbServerInfoRepo {

    /**
     * @see SmbServerInfoRepo.getAllSmbServersAscStream
     */
    override fun getAllSmbServersAscStream(): Flow<List<SmbServerInfo>> =
        smbServerDao.getByAddedDate()

    /**
     * @see SmbServerInfoRepo.getSmbServerStream
     */
    override fun getSmbServerStream(smbServerId: Long): Flow<SmbServerInfo?> = smbServerDao.getByIdStream(smbServerId)

    /**
     * @see SmbServerInfoRepo.getSmbServer
     */
    override suspend fun getSmbServer(id: Long): SmbServerInfo = smbServerDao.getById(id)

    /**
     * @see SmbServerInfoRepo.upsertSmbServer
     */
    override suspend fun upsertSmbServer(smbServerInfo: SmbServerInfo) =
        smbServerDao.upsert(smbServerInfo)

    /**
     * @see SmbServerInfoRepo.deleteSmbServer
     */
    override suspend fun deleteSmbServer(smbServerInfo: SmbServerInfo) =
        smbServerDao.delete(smbServerInfo)
}
