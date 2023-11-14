package com.ad.backupfiles.data.repository

import android.database.SQLException
import android.util.Log
import androidx.room.Transaction
import com.ad.backupfiles.data.dao.DirectoryDao
import com.ad.backupfiles.data.entity.DirectoryDto
import com.ad.backupfiles.data.entity.DirectoryInfo
import com.ad.backupfiles.data.entity.DirectorySyncInfo
import com.ad.backupfiles.data.entity.toDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.time.Instant

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

class OfflineDirectoryRepo(
    private val directoryDao: DirectoryDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : DirectoryRepo {
    private val TAG: String = OfflineDirectoryRepo::class.java.simpleName

    /**
     * @see DirectoryRepo.getAllSavedDirectoriesStream
     */
    override fun getAllSavedDirectoriesStream(smbServerId: Long) =
        directoryDao.getSmbServerWithDirectories(smbServerId)

    override suspend fun isDirectorySaved(smbServerId: Long, dirPath: String): Boolean =
        directoryDao.isDirectorySaved(smbServerId, dirPath)

    /**
     * @see DirectoryRepo.insertDirectory
     */
    @Transaction
    override suspend fun insertDirectory(dir: DirectoryInfo): Long? {
        return try {
            directoryDao.insertAndQueueForBackup(dir)
        } catch (e: SQLException) {
            Log.e("insertDirectory", "Failed to insert directory: ${e.message}", e.cause)
            null
        }
    }

    /**
     * @see DirectoryRepo.deleteDirectory
     */
    override suspend fun deleteDirectory(dir: DirectoryInfo) {
        withContext(ioDispatcher) {
            directoryDao.delete(dir)
        }
    }

    /**
     * @see DirectoryRepo.getDirectory
     */
    override suspend fun getDirectory(dirId: Long, smbServerId: Long): DirectoryDto? {
        return directoryDao.getDirectoryById(dirId, smbServerId)?.toDto()
    }

    /**
     * @see DirectoryRepo.getPendingSyncDirectories
     */
    override fun getPendingSyncDirectories(smbServerId: Long) = flow {
        directoryDao.getPendingSyncDirectories(smbServerId).onEach {
            emit(it)
        }
    }

    /**
     * @see DirectoryRepo.insertDirectoriesToSync
     */
    override suspend fun insertDirectoriesToSync(smbServerId: Long, directoryIds: MutableList<Long>) {
        withContext(ioDispatcher) {
            directoryIds.asFlow().mapNotNull { dirId ->
                directoryDao.getDirectoryById(dirId, smbServerId)
            }.map { dirInfo ->
                DirectorySyncInfo(dirId = dirInfo.dirId, smbServerId = dirInfo.smbServerId, dirPath = dirInfo.dirPath)
            }.onEach { directorySyncInfo ->
                directoryDao.insertDirectoryForSync(directorySyncInfo)
            }.catch { exception ->
                // If we failed to insert, then stop processing rest of the data
                Log.e(TAG, "Unable to queue directory for sync-up: $exception")
            }.collect()
        }
    }

    /**
     * @see DirectoryRepo.processSyncedDirectory
     */
    @Transaction
    override suspend fun processSyncedDirectory(syncedDirectory: DirectorySyncInfo) {
        withContext(ioDispatcher) {
            directoryDao.updateSyncTime(syncedDirectory.dirId, syncedDirectory.smbServerId, Instant.now().epochSecond)
            directoryDao.removeFromSync(syncedDirectory)
        }
    }
}