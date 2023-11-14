package com.ad.backupfiles.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ad.backupfiles.data.dao.DirectoryDao
import com.ad.backupfiles.data.dao.SmbServerDao
import com.ad.backupfiles.data.entity.DirectoryInfo
import com.ad.backupfiles.data.entity.DirectorySyncInfo
import com.ad.backupfiles.data.entity.SmbServerInfo

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

@Database(
    entities = [SmbServerInfo::class, DirectoryInfo::class, DirectorySyncInfo::class],
    version = 1,
    exportSchema = false
)
abstract class BackupFilesDatabase : RoomDatabase() {
    abstract fun smbServerDao(): SmbServerDao
    abstract fun directoryDao(): DirectoryDao

    companion object {
        private var Instance: BackupFilesDatabase? = null
        fun getDatabase(context: Context): BackupFilesDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, BackupFilesDatabase::class.java, "backup_file")
                    /**
                     * TODO: setup migration..
                     * Setting this option in app's database builder means that Room
                     * permanently deletes all data from the tables in your database when it
                     * attempts to perform a migration with no defined migration path.
                     */
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}