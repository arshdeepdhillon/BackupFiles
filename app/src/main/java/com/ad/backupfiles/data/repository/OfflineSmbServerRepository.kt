package com.ad.backupfiles.data.repository

import com.ad.backupfiles.data.dao.SmbServerDao
import com.ad.backupfiles.data.entity.SmbServerInfo
import kotlinx.coroutines.flow.Flow

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

class OfflineSmbServerRepository(private val smbServerDao: SmbServerDao) : SmbServerInfoRepository {

    /**
     * @see SmbServerInfoRepository.getAllSmbServersAscStream
     */
    override fun getAllSmbServersAscStream(): Flow<List<SmbServerInfo>> =
        smbServerDao.getByAddedDate()

    /**
     * @see SmbServerInfoRepository.getSmbServerStream
     */
    override fun getSmbServerStream(id: Int): Flow<SmbServerInfo?> = smbServerDao.getByIdStream(id)

    /**
     * @see SmbServerInfoRepository.getSmbServer
     */
    override suspend fun getSmbServer(id: Int): SmbServerInfo = smbServerDao.getById(id)

    /**
     * @see SmbServerInfoRepository.upsertSmbServer
     */
    override suspend fun upsertSmbServer(smbServerInfo: SmbServerInfo) =
        smbServerDao.upsert(smbServerInfo)

    /**
     * @see SmbServerInfoRepository.deleteSmbServer
     */
    override suspend fun deleteSmbServer(smbServerInfo: SmbServerInfo) =
        smbServerDao.delete(smbServerInfo)
}