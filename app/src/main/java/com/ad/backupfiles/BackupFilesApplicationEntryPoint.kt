package com.ad.backupfiles

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.ad.backupfiles.data.AppContainer
import com.ad.backupfiles.data.AppDataContainer
import com.ad.backupfiles.worker.CHANNEL_DESC
import com.ad.backupfiles.worker.CHANNEL_ID
import com.ad.backupfiles.worker.CHANNEL_NAME

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */
class BackupFilesApplicationEntryPoint : Application() {

    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()

        createNotificationChannels()
        container = AppDataContainer(this)
    }

    /**
     * Initializes the necessary channels required for Notifications
     */
    private fun createNotificationChannels() {
        // Create the NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_NAME
            val descriptionText = CHANNEL_DESC
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val nM: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nM.createNotificationChannel(channel)
        }
    }
}