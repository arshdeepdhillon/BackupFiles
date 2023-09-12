package com.ad.syncfiles.background.workers

import android.content.Context
import android.os.FileObserver
import androidx.work.Worker
import androidx.work.WorkerParameters

class FolderMonitorWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    private lateinit var fileObserver: FileObserver
    override fun doWork(): Result {
        fileObserver = object : FileObserver("someUri") {
            override fun onEvent(p0: Int, p1: String?) {
                TODO("Not yet implemented")
            }

        }
        return Result.failure()
    }
}