package com.ad.syncfiles.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters


class BackupFilesWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        println("Started BackupFilesWorker!")
        val dirUriInput =
            inputData.getString("DIR_URI") ?: return Result.failure()

        val appCtx = applicationContext
        makeStatusNotification("Getting files", appCtx)
//        sleep(2000)
//        makeStatusNotification("Saving Files", appCtx)
//        sleep(1000)
//        makeStatusNotification("Done!", appCtx)

        return Result.success()
    }
}