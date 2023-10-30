package com.ad.backupfiles.data

import android.content.Context
import com.ad.backupfiles.data.repository.OfflineSaveDirectoryRepository
import com.ad.backupfiles.data.repository.OfflineSmbServerRepository
import com.ad.backupfiles.data.repository.SaveDirectoryRepository
import com.ad.backupfiles.data.repository.SmbServerInfoRepository

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Application container for Dependency Injection
 */
interface AppContainer {
    val smbServerRepository: SmbServerInfoRepository
    val saveDirectoryRepository: SaveDirectoryRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    /**
     * Implementation for [SmbServerInfoRepository]
     */
    override val smbServerRepository: SmbServerInfoRepository by lazy {
        OfflineSmbServerRepository(BackupFilesDatabase.getDatabase(context).smbServerDao())
    }

    /**
     * Implementation for [SaveDirectoryRepository]
     */
    override val saveDirectoryRepository: SaveDirectoryRepository by lazy {
        OfflineSaveDirectoryRepository(BackupFilesDatabase.getDatabase(context).savedDirDao())
    }
}