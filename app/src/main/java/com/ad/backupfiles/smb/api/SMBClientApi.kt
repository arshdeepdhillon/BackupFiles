package com.ad.backupfiles.smb.api

import android.content.Context
import com.ad.backupfiles.data.entity.SmbServerDto

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * An interface for interacting with SMB servers.
 */
interface SMBClientApi {

    /**
     * Attempts to establish a connection with an SMB server to check if it's reachable.
     *
     * @param smbServerDto The SMB server details to be used for the connection attempt.
     * @return `true` if the connection can be established; `false` otherwise.
     */
    suspend fun canConnect(smbServerDto: SmbServerDto): Boolean

    /**
     * Copies the files and its TODO->subdirectories<- from the specified [uri] and saves it to an SMB server.
     *
     * @param context The context used for accessing resources and file operations.
     * @param smbServerDto The SMB server details to which the folder should be saved.
     * @param uri The Uri of the folder to be saved.
     * @param isSync backup the missing content of [smbServerDto] on the server if true, otherwise do normal backup
     */
    suspend fun saveFolder(context: Context, smbServerDto: SmbServerDto, folderToSave: String, isSync: Boolean = false)
}
