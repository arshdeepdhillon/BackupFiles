package com.ad.syncfiles.data

import android.content.Context


interface AppContainer {
    val serverInfoRepo: ServerInfoRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    /**
     * Implementation for [ServerInfoRepository]
     */
    override val serverInfoRepo: ServerInfoRepository by lazy {
        OfflineServerInfoRepository(ServerInfoDatabase.getDatabase(context).serverInfoDao())
    }
}