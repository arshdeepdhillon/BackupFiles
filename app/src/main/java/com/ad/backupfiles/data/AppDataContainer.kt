package com.ad.backupfiles.data

import android.content.Context
import com.ad.backupfiles.data.repository.DirectoryRepo
import com.ad.backupfiles.data.repository.OfflineDirectoryRepo
import com.ad.backupfiles.data.repository.OfflineSmbServerRepo
import com.ad.backupfiles.data.repository.SmbServerInfoRepo

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
        OfflineDirectoryRepo(BackupFilesDatabase.getDatabase(context).directoryDao())
    }
}