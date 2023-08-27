package com.ad.syncfiles.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ServerInfo::class], version = 1, exportSchema = false)
abstract class ServerInfoDatabase : RoomDatabase() {
    abstract fun serverInfoDao(): ServerInfoDao

    companion object {
        private var Instance: ServerInfoDatabase? = null
        fun getDatabase(context: Context): ServerInfoDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, ServerInfoDatabase::class.java, "server_info")
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