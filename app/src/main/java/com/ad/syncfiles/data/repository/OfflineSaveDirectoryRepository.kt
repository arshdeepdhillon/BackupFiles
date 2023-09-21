package com.ad.syncfiles.data.repository

import com.ad.syncfiles.data.dao.SavedDirectoryDao
import com.ad.syncfiles.data.entity.DirectoryInfo

class OfflineSaveDirectoryRepository(private val savedDirectoryDao: SavedDirectoryDao) : SaveDirectoryRepository {

    /**
     * @see SaveDirectoryRepository.getAllSavedDirectoriesStream
     */
    override fun getAllSavedDirectoriesStream(smbServerId: Int) =
        savedDirectoryDao.getSmbServerWithDirectories(smbServerId)

    /**
     * @see SaveDirectoryRepository.upsertDirectory
     */
    override suspend fun upsertDirectory(dir: DirectoryInfo) = savedDirectoryDao.upsert(dir)

    /**
     * @see SaveDirectoryRepository.deleteDirectory
     */
    override suspend fun deleteDirectory(dir: DirectoryInfo) = savedDirectoryDao.delete(dir)

}