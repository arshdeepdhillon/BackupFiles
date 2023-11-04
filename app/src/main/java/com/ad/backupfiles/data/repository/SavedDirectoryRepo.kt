package com.ad.backupfiles.data.repository

import com.ad.backupfiles.data.entity.DirectoryDto
import com.ad.backupfiles.data.entity.DirectoryInfo
import com.ad.backupfiles.data.entity.SMBServerWithSavedDirs
import kotlinx.coroutines.flow.Flow

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Repository that provides access to backed up [DirectoryInfo]s of SMB servers.
 */
interface SavedDirectoryRepo {

    /**
     * Retrieves a flow of lists containing saved directories associated with the specified SMB server.
     *
     * @param smbServerId The unique identifier of the SMB server for which to retrieve saved directories.
     *
     * @return A [Flow] emitting [DirectoryInfo] objects which represents saved directories.
     * The flow emits updates whenever the saved directories associated with the specified SMB server change.
     */
    fun getAllSavedDirectoriesStream(smbServerId: Int): Flow<SMBServerWithSavedDirs>

    /**
     * Checks if [dirPath] is saved for [smbServerId].
     *
     * @param smbServerId The unique identifier of the SMB server to query.
     *
     * @param dirPath The path of directory to check.
     *
     * @return true if the folder is already saved otherwise false.
     */
    suspend fun isDirectorySaved(smbServerId: Int, dirPath: String): Boolean

    /**
     * Inserts or updates a [DirectoryInfo] object in the data source. If the specified [dir] already exists,
     * it will be updated; otherwise, a new directory entry will be inserted.
     *
     * @param dir The [DirectoryInfo] object to insert or update.
     * @return id of the newly inserted Directory
     */
    suspend fun insertDirectory(dir: DirectoryInfo): Long

    /**
     * Deletes a [DirectoryInfo] specified by [dir].
     *
     * @param dir The [DirectoryInfo] object to be deleted.
     */
    suspend fun deleteDirectory(dir: DirectoryInfo)


    /**
     * Updates the sync time for given directory to [java.time.Instant.now] seconds.
     *
     * @param dirId The identifier of the directory to update.
     * @param smbId The identifier of the SMB server associated with the directory.
     */
    suspend fun updateSyncTime(dirId: Long, smbId: Int)

    /**
     * Retrieves a directory using given [dirId] and [smbId].
     *
     * @param dirId The identifier of the directory.
     * @param smbId The identifier of the SMB server associated with the directory.
     * @return [DirectoryDto] representing the directory, or null if not found.
     */
    suspend fun getDir(dirId: Long, smbId: Int): DirectoryDto?
}