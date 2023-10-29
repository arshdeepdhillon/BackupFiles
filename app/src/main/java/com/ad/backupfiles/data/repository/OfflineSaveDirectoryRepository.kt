package com.ad.backupfiles.data.repository

import com.ad.backupfiles.data.dao.SavedDirectoryDao
import com.ad.backupfiles.data.entity.DirectoryInfo

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

class OfflineSaveDirectoryRepository(private val savedDirectoryDao: SavedDirectoryDao) :
    SaveDirectoryRepository {

    /**
     * @see SaveDirectoryRepository.getAllSavedDirectoriesStream
     */
    override fun getAllSavedDirectoriesStream(smbServerId: Int) =
        savedDirectoryDao.getSmbServerWithDirectories(smbServerId)

    override suspend fun isDirectorySaved(smbServerId: Int, dirPath: String): Boolean =
        savedDirectoryDao.isDirectorySaved(smbServerId, dirPath)

    /**
     * @see SaveDirectoryRepository.upsertDirectory
     */
    override suspend fun upsertDirectory(dir: DirectoryInfo) = savedDirectoryDao.upsert(dir)

    /**
     * @see SaveDirectoryRepository.deleteDirectory
     */
    override suspend fun deleteDirectory(dir: DirectoryInfo) = savedDirectoryDao.delete(dir)

}