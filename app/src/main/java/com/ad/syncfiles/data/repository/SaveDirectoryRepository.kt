package com.ad.syncfiles.data.repository

import com.ad.syncfiles.data.entity.DirectoryInfo
import com.ad.syncfiles.data.entity.SMBServerWithSavedDirs
import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides access to backed up [DirectoryInfo]s of SMB servers.
 */
interface SaveDirectoryRepository {

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
     * Inserts or updates a [DirectoryInfo] object in the data source. If the specified [dir] already exists,
     * it will be updated; otherwise, a new directory entry will be inserted.
     *
     * @param dir The [DirectoryInfo] object to insert or update.
     */
    suspend fun upsertDirectory(dir: DirectoryInfo)

    /**
     * Deletes a [DirectoryInfo] specified by [dir].
     *
     * @param dir The [DirectoryInfo] object to be deleted.
     */
    suspend fun deleteDirectory(dir: DirectoryInfo)
}
