package com.ad.backupfiles.di.api

import android.content.Context
import com.ad.backupfiles.data.repository.api.DirectoryInfoApi
import com.ad.backupfiles.data.repository.api.SmbServerInfoApi
import com.ad.backupfiles.smb.api.SMBClientApi

/*
 * @author : Arshdeep Dhillon
 * @created : 09-Dec-23
*/

/**
 * Application container that provides dependencies for the application.
 *
 * @property appContext The Android application context used for various operations within the application.
 * @property smbServerApi The API for interacting with saved SMB server information.
 * @property directoryInfoApi The API for interacting with the saved directory information.
 * @property smbClientApi The API for interacting with SMB servers, providing methods for SMB-related operations.
 */
interface ApplicationModuleApi {
    val appContext: Context
    val smbServerApi: SmbServerInfoApi
    val directoryInfoApi: DirectoryInfoApi
    val smbClientApi: SMBClientApi
}
