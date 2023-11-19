package com.ad.backupfiles.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import com.ad.backupfiles.worker.CANCEL_ACTION_KEY

/*
 * @author : Arshdeep Dhillon
 * @created : 14-Nov-23
*/

/**
 * BroadcastReceiver for handling cancellation of upload tasks.
 *
 * This receiver is designed to listen for broadcasts with a specified action key [CANCEL_ACTION_KEY]
 * and cancels all WorkManager tasks associated with the provided worker tag.
 */
class UploadCancelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        intent.getStringExtra(CANCEL_ACTION_KEY)?.let { workerTag: String ->
            WorkManager.getInstance(context).cancelAllWorkByTag(workerTag)
        }
    }
}