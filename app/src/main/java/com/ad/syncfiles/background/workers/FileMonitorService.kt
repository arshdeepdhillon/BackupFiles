package com.ad.syncfiles.background.workers

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.ad.syncfiles.R

class FileMonitorService : Service() {
    companion object {
        const val FILE_MONITOR_CHANNEL_ID = "MONITOR_FILES"
        const val FILE_MONITOR_NOTIFICATION_ID = 1

    }

    override fun onBind(intent: Intent): IBinder? {
        TODO("Return the communication channel to the service.")
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "service starting: [${intent?.action}]", Toast.LENGTH_SHORT).show()
        when (intent?.action) {
            FileMonitorAction.START.toString() -> start()
            FileMonitorAction.STOP.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {


        /**
         * Need persistent notification inorder for this service to run in foreground
         */
        val nm: Notification = Notification.Builder(this, FILE_MONITOR_CHANNEL_ID)
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .setContentIntent(pendingIntent)
            .build()
        startForeground(FILE_MONITOR_NOTIFICATION_ID, nm)

    }
}