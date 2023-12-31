package com.ad.backupfiles

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.ad.backupfiles.di.ApplicationModuleImpl
import com.ad.backupfiles.di.api.ApplicationModuleApi
import com.ad.backupfiles.worker.DETAILED_CHANNEL_DESC
import com.ad.backupfiles.worker.DETAILED_CHANNEL_ID
import com.ad.backupfiles.worker.DETAILED_CHANNEL_NAME
import com.ad.backupfiles.worker.MAIN_CHANNEL_DESC
import com.ad.backupfiles.worker.MAIN_CHANNEL_ID
import com.ad.backupfiles.worker.MAIN_CHANNEL_NAME
import org.jetbrains.annotations.TestOnly

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */
class AppEntryPoint : Application() {

    companion object {
        lateinit var appModule: ApplicationModuleApi

        @TestOnly
        fun setAppModuleForTest(testAppModule: ApplicationModuleApi) {
            appModule = testAppModule
        }
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannels()

        // Initialized in the Application so it remains alive as long as the application.
        appModule = ApplicationModuleImpl(this)
    }

    /**
     * Initializes the necessary channels required for Notifications
     */
    private fun createNotificationChannels() {
        // Create the NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mainChannel = NotificationChannel(
                MAIN_CHANNEL_ID,
                MAIN_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = MAIN_CHANNEL_DESC
            }
            val detailedChannel = NotificationChannel(
                DETAILED_CHANNEL_ID,
                DETAILED_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = DETAILED_CHANNEL_DESC
            }
            // Register the channel with the system
            val nM: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nM.createNotificationChannel(mainChannel)
            nM.createNotificationChannel(detailedChannel)
        }
    }
}
