package com.ad.backupfiles.data

import android.content.Context
import com.ad.backupfiles.data.repository.DirectoryRepo
import com.ad.backupfiles.data.repository.OfflineDirectoryRepo
import com.ad.backupfiles.data.repository.OfflineSmbServerRepo
import com.ad.backupfiles.data.repository.SmbServerInfoRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Application container for Dependency Injection
 */
interface AppContainer {
    val smbServerRepo: SmbServerInfoRepo
    val directoryRepo: DirectoryRepo
}

class AppDataContainer(private val context: Context) : AppContainer {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Implementation for [SmbServerInfoRepo]
     */
    override val smbServerRepo: SmbServerInfoRepo by lazy {
        OfflineSmbServerRepo(BackupFilesDatabase.getDatabase(context).smbServerDao())
    }

    /**
     * Implementation for [DirectoryRepo]
     */
    override val directoryRepo: DirectoryRepo by lazy {
        OfflineDirectoryRepo(BackupFilesDatabase.getDatabase(context).directoryDao(), applicationScope)
    }
}