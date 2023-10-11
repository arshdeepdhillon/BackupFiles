package com.ad.syncfiles.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ad.syncfiles.R

const val CHANNEL_ID = "BACKUP_NOTIFICATION"
const val CHANNEL_NAME = "Backup Information"
const val CHANNEL_DESC = "Displays Live Backup Information"

const val NOTIFICATION_ID = 1
val NOTIFICATION_TITLE: CharSequence = "Backing Up Data"


// The name of the backup work
const val BACK_UP_FILES_WORK_NAME = "backup_files_work"

fun makeStatusNotification(message: String, ctx: Context) {
    // Check notification is enabled before creating it.
    if (NotificationManagerCompat.from(ctx).areNotificationsEnabled()) {
        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))

        // Show the notification
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, builder.build())
        }
    }
}




