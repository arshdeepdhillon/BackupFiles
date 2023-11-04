package com.ad.backupfiles.data.repository

import com.ad.backupfiles.data.dao.SavedDirectoryDao
import com.ad.backupfiles.data.entity.DirectoryDto
import com.ad.backupfiles.data.entity.DirectoryInfo
import com.ad.backupfiles.data.entity.toDto
import java.time.Instant

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

class OfflineSavedDirectoryRepo(private val savedDirectoryDao: SavedDirectoryDao) : SavedDirectoryRepo {

    /**
     * @see SavedDirectoryRepo.getAllSavedDirectoriesStream
     */
    override fun getAllSavedDirectoriesStream(smbServerId: Int) =
        savedDirectoryDao.getSmbServerWithDirectories(smbServerId)

    override suspend fun isDirectorySaved(smbServerId: Int, dirPath: String): Boolean =
        savedDirectoryDao.isDirectorySaved(smbServerId, dirPath)

    /**
     * @see SavedDirectoryRepo.insertDirectory
     */
    override suspend fun insertDirectory(dir: DirectoryInfo): Long = savedDirectoryDao.insert(dir)

    /**
     * @see SavedDirectoryRepo.deleteDirectory
     */
    override suspend fun deleteDirectory(dir: DirectoryInfo) = savedDirectoryDao.delete(dir)


    /**
     * @see SavedDirectoryRepo.updateSyncTime
     */
    override suspend fun updateSyncTime(dirId: Long, smbId: Int) {
        savedDirectoryDao.updateSyncTime(dirId, smbId, Instant.now().epochSecond)
    }

    /**
     * @see SavedDirectoryRepo.getDir
     */
    override suspend fun getDir(dirId: Long, smbId: Int): DirectoryDto? {
        return savedDirectoryDao.getById(dirId, smbId)?.toDto()
    }
}