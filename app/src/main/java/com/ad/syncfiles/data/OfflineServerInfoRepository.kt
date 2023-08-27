package com.ad.syncfiles.data

import kotlinx.coroutines.flow.Flow

class OfflineServerInfoRepository(private val serverInfoDao: ServerInfoDao) : ServerInfoRepository {

    override fun getAllSharedServersAscStream(): Flow<List<ServerInfo>> = serverInfoDao.getByAddedDate()

    override fun getServerInfoStream(id: Int): Flow<ServerInfo?> = serverInfoDao.getById(id)

    override suspend fun upsertServerInfo(serverInfo: ServerInfo) = serverInfoDao.upsert(serverInfo)

    override suspend fun deleteServerInfo(serverInfo: ServerInfo) = serverInfoDao.delete(serverInfo)
}