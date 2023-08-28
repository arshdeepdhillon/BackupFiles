package com.ad.syncfiles.data

import android.content.Context
import com.ad.syncfiles.data.repository.OfflineSmbServerRepository
import com.ad.syncfiles.data.repository.SmbServerInfoRepository

/**
 * Application container for Dependency Injection
 */
interface AppContainer {
    val smbServerRepository: SmbServerInfoRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    /**
     * Implementation for [SmbServerInfoRepository]
     */
    override val smbServerRepository: SmbServerInfoRepository by lazy {
        OfflineSmbServerRepository(SmbServerDatabase.getDatabase(context).smbServerDao())
    }
}