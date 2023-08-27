package com.ad.syncfiles

import android.app.Application
import com.ad.syncfiles.data.AppContainer
import com.ad.syncfiles.data.AppDataContainer

class SyncFilesApplicationEntryPoint : Application() {

    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)

    }
}