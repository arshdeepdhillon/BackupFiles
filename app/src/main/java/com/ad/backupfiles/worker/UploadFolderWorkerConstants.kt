package com.ad.backupfiles.worker

/*
 * @author : Arshdeep Dhillon
 * @created : 22-Dec-23
*/
object UploadFolderWorkerConstants {
    const val WORKER_TYPE_TAG = "worker_type_tag"
    const val MAX_RETRY = 3

    const val WORK_NAME = "upload_folder_work"

    // Required for both backup and sync work
    const val SMB_ID_KEY = "SMB_SERVER_ID"
}

/**
 * Enum representing the types of worker operations available for [UploadFolderWorker].
 */
enum class WorkerType {
    /**
     * Represents the backup worker type.
     */
    BACKUP,

    /**
     * Represents the sync worker type.
     */
    SYNC,
}
