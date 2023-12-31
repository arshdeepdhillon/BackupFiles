package com.ad.backupfiles.di

import android.content.Context
import com.ad.backupfiles.data.BackupFilesDatabase
import com.ad.backupfiles.data.repository.OfflineDirectoryRepoImpl
import com.ad.backupfiles.data.repository.OfflineSmbServerRepoImpl
import com.ad.backupfiles.data.repository.api.DirectoryInfoApi
import com.ad.backupfiles.data.repository.api.SmbServerInfoApi
import com.ad.backupfiles.di.api.ApplicationModuleApi
import com.ad.backupfiles.smb.SMBClientImpl
import com.ad.backupfiles.smb.api.SMBClientApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Default implementation of the [ApplicationModuleApi] interface which provides dependencies for the application.
 *
 * @property context The Android application context used for various operations within the application.
 * @property defaultDispatcher The coroutine dispatcher to be used for background tasks (default is [Dispatchers.Default]).
 */
class ApplicationModuleImpl(private val context: Context, private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default) : ApplicationModuleApi {
    /**
     * The coroutine scope associated with the application.
     */
    private val applicationScope = CoroutineScope(SupervisorJob() + defaultDispatcher)

    /**
     * @see ApplicationModuleApi.smbServerApi
     */
    override val smbServerApi: SmbServerInfoApi by lazy {
        OfflineSmbServerRepoImpl(BackupFilesDatabase.getDatabase(context).smbServerDao())
    }

    /**
     * @see ApplicationModuleApi.directoryInfoApi
     */
    override val directoryInfoApi: DirectoryInfoApi by lazy {
        OfflineDirectoryRepoImpl(BackupFilesDatabase.getDatabase(context).directoryDao(), applicationScope)
    }

    /**
     * @see ApplicationModuleApi.appContext
     */
    override val appContext: Context by lazy {
        context
    }

    /**
     * @see ApplicationModuleApi.smbClientApi
     */
    override val smbClientApi: SMBClientApi by lazy {
        SMBClientImpl()
    }
}
