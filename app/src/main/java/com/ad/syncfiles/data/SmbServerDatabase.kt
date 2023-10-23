package com.ad.syncfiles.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ad.syncfiles.data.dao.SavedDirectoryDao
import com.ad.syncfiles.data.dao.SmbServerDao
import com.ad.syncfiles.data.entity.DirectoryInfo
import com.ad.syncfiles.data.entity.SmbServerInfo

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

@Database(
    entities = [SmbServerInfo::class, DirectoryInfo::class],
    version = 1,
    exportSchema = false
)
abstract class SmbServerDatabase : RoomDatabase() {
    abstract fun smbServerDao(): SmbServerDao
    abstract fun savedDirDao(): SavedDirectoryDao

    companion object {
        private var Instance: SmbServerDatabase? = null
        fun getDatabase(context: Context): SmbServerDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, SmbServerDatabase::class.java, "smb_server_info")
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