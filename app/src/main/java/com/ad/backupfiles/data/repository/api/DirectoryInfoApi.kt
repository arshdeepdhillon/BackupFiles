package com.ad.backupfiles.data.repository.api

import com.ad.backupfiles.data.entity.DirectoryDto
import com.ad.backupfiles.data.entity.DirectoryInfo
import com.ad.backupfiles.data.entity.DirectorySyncInfo
import com.ad.backupfiles.data.entity.SMBServerWithSavedDirs
import kotlinx.coroutines.flow.Flow

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Repository that provides access to backed up [DirectoryInfo]s of SMB servers and [DirectorySyncInfo].
 */
interface DirectoryInfoApi {

    /**
     * Retrieves a flow of lists containing saved directories associated with the specified SMB server.
     *
     * @param smbServerId The unique identifier of the SMB server for which to retrieve saved directories.
     *
     * @return A [Flow] emitting [DirectoryInfo] objects which represents saved directories.
     * The flow emits updates whenever the saved directories associated with the specified SMB server change.
     */
    fun getAllSavedDirectoriesStream(smbServerId: Long): Flow<SMBServerWithSavedDirs>

    /**
     * Checks if [dirPath] is saved for [smbServerId].
     *
     * @param smbServerId The unique identifier of the SMB server to query.
     *
     * @param dirPath The path of directory to check.
     *
     * @return true if the folder is already saved otherwise false.
     */
    suspend fun isDirectorySaved(smbServerId: Long, dirPath: String): Boolean

    /**
     * Inserts or updates a [DirectoryInfo] object in the data source. If the specified [dir] already exists,
     * it will be updated; otherwise, a new directory entry will be inserted.
     *
     * @param dir The [DirectoryInfo] object to insert or update.
     * @return id of the newly inserted Directory, otherwise null
     */
    suspend fun insertDirectory(dir: DirectoryInfo): Long?

    /**
     * Deletes a [DirectoryInfo] specified by [dir].
     *
     * @param dir The [DirectoryInfo] object to be deleted.
     */
    suspend fun deleteDirectory(dir: DirectoryInfo)

    /**
     * Retrieves a directory using given [dirId] and [smbServerId].
     *
     * @param dirId The identifier of the directory.
     * @param smbServerId The identifier of the SMB server associated with the directory.
     * @return [DirectoryDto] representing the directory, or null if not found.
     */
    suspend fun getDirectory(dirId: Long, smbServerId: Long): DirectoryDto?

    // START: Sync related operations
    /**
     * Synchronously queues the directories for synchronization with the SMB server.
     *
     * @param smbServerId The ID of the SMB server associated with the directories.
     * @param directoryIds The list of directory IDs to synchronize.
     */
    suspend fun insertDirectoriesToSync(smbServerId: Long, directoryIds: MutableList<Long>)

    /**
     * Retrieves directories to sync for the specified SMB server ID in the form of a Flow.
     *
     * @param smbServerId The ID of the SMB server.
     * @return A Flow emitting directories to sync
     */
    fun getPendingSyncDirectories(smbServerId: Long): Flow<DirectorySyncInfo>

    /**
     * Updates the last synchronized timestamp and removes it from the queue (aka database).
     *
     * @param syncedDirectory to process
     */
    suspend fun processSyncedDirectory(syncedDirectory: DirectorySyncInfo)

    /**
     * Deletes all directories to sync for the specified SMB server ID.
     *
     * @param smbServerId The ID of the SMB server.
     */
    suspend fun deleteAllPendingSyncDirectories(smbServerId: Long)

    // END: Sync related operations
}
