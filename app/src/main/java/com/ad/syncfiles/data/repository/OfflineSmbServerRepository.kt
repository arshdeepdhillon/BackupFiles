package com.ad.syncfiles.data.repository

import com.ad.syncfiles.data.dao.SmbServerDao
import com.ad.syncfiles.data.entity.SmbServerInfo
import kotlinx.coroutines.flow.Flow

class OfflineSmbServerRepository(private val smbServerDao: SmbServerDao) : SmbServerInfoRepository {

    override fun getAllSmbServersAscStream(): Flow<List<SmbServerInfo>> = smbServerDao.getByAddedDate()

    override fun getSmbServerStream(id: Int): Flow<SmbServerInfo?> = smbServerDao.getById(id)

    override suspend fun upsertSmbServer(smbServerInfo: SmbServerInfo) = smbServerDao.upsert(smbServerInfo)

    override suspend fun deleteSmbServer(smbServerInfo: SmbServerInfo) = smbServerDao.delete(smbServerInfo)
}