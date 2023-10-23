package com.ad.syncfiles.data

import android.content.Context
import com.ad.syncfiles.data.repository.OfflineSaveDirectoryRepository
import com.ad.syncfiles.data.repository.OfflineSmbServerRepository
import com.ad.syncfiles.data.repository.SaveDirectoryRepository
import com.ad.syncfiles.data.repository.SmbServerInfoRepository

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
        OfflineSmbServerRepository(SmbServerDatabase.getDatabase(context).smbServerDao())
    }

    /**
     * Implementation for [SaveDirectoryRepository]
     */
    override val saveDirectoryRepository: SaveDirectoryRepository by lazy {
        OfflineSaveDirectoryRepository(SmbServerDatabase.getDatabase(context).savedDirDao())
    }
}