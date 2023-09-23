package com.ad.syncfiles.data.repository

import com.ad.syncfiles.data.dao.SmbServerDao
import com.ad.syncfiles.data.entity.SmbServerInfo
import kotlinx.coroutines.flow.Flow

class OfflineSmbServerRepository(private val smbServerDao: SmbServerDao) : SmbServerInfoRepository {

    /**
     * @see SmbServerInfoRepository.getAllSmbServersAscStream
     */
    override fun getAllSmbServersAscStream(): Flow<List<SmbServerInfo>> = smbServerDao.getByAddedDate()

    /**
     * @see SmbServerInfoRepository.getSmbServerStream
     */
    override fun getSmbServerStream(id: Int): Flow<SmbServerInfo?> = smbServerDao.getById(id)

    /**
     * @see SmbServerInfoRepository.upsertSmbServer
     */
    override suspend fun upsertSmbServer(smbServerInfo: SmbServerInfo) = smbServerDao.upsert(smbServerInfo)

    /**
     * @see SmbServerInfoRepository.deleteSmbServer
     */
    override suspend fun deleteSmbServer(smbServerInfo: SmbServerInfo) = smbServerDao.delete(smbServerInfo)
}