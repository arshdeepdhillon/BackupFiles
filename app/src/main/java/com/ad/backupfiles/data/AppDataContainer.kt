package com.ad.backupfiles.data

import android.content.Context
import com.ad.backupfiles.data.repository.OfflineSavedDirectoryRepo
import com.ad.backupfiles.data.repository.OfflineSmbServerRepo
import com.ad.backupfiles.data.repository.SavedDirectoryRepo
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
    val directoryRepo: SavedDirectoryRepo
}

class AppDataContainer(private val context: Context) : AppContainer {

    /**
     * Implementation for [SmbServerInfoRepo]
     */
    override val smbServerRepo: SmbServerInfoRepo by lazy {
        OfflineSmbServerRepo(BackupFilesDatabase.getDatabase(context).smbServerDao())
    }

    /**
     * Implementation for [SavedDirectoryRepo]
     */
    override val directoryRepo: SavedDirectoryRepo by lazy {
        OfflineSavedDirectoryRepo(BackupFilesDatabase.getDatabase(context).savedDirDao())
    }
}