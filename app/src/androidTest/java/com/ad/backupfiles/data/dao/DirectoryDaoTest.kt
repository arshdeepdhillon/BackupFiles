package com.ad.backupfiles.data.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.ad.backupfiles.TestDispatcherRule
import com.ad.backupfiles.data.BackupFilesDatabase
import com.ad.backupfiles.data.entity.DirectoryInfo
import com.ad.backupfiles.data.entity.SmbServerInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

/*
 * @author : Arshdeep Dhillon
 * @created : 10-Dec-23
*/
@OptIn(ExperimentalCoroutinesApi::class)
@MediumTest
class DirectoryDaoTest {

    private lateinit var backupDb: BackupFilesDatabase
    private lateinit var dirDao: DirectoryDao
    private lateinit var smbDao: SmbServerDao
    private val testDispatcher = StandardTestDispatcher(TestCoroutineScheduler())

    @get: Rule
    val dispatcherRule = TestDispatcherRule(testDispatcher)

    @Before
    fun setup() {
        // Setup in-memory database for testing
        backupDb = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BackupFilesDatabase::class.java).build()
        smbDao = backupDb.smbServerDao()
        dirDao = backupDb.directoryDao()

        runTest {
            smbs.forEach { smbDao.upsert(it) }
            checkNoDirsInSmbs()
        }
    }

    companion object {
        private lateinit var dirs: ArrayList<DirectoryInfo>
        private lateinit var smbs: ArrayList<SmbServerInfo>

        @JvmStatic
        @BeforeClass
        fun init() {
            smbs = (1..5).map { i ->
                SmbServerInfo(
                    smbServerId = i.toLong(),
                    serverAddress = "192.168.100.$i",
                    username = "test_username_$i",
                    password = "testpassword_$i",
                    sharedFolderName = "share_folder_$i",
                )
            }.toCollection(ArrayList())

            // Each SMB server will have at least one saved directory
            dirs = smbs.map { smb ->
                DirectoryInfo(
                    dirId = smb.smbServerId,
                    smbServerId = smb.smbServerId,
                    dirPath = "some_uri_path",
                    dirName = "filename_${smb.smbServerId}.jpg",
                    lastSynced = null,
                )
            }.toCollection(ArrayList())
        }
    }

    @After
    fun tearDown() {
        backupDb.close()
    }

    private suspend fun checkNoDirsInSmbs() {
        smbs.forEach { smb ->
            dirDao.getSmbServerWithDirectories(smb.smbServerId).test {
                val lists = awaitItem()
                assert(lists.savedDirs.isEmpty())
                cancel()
            }
        }
    }

    @Test
    fun test_error_thrown_when_saving_same_directory() = runTest {
        var e: Exception? = null
        async {
            e = try {
                dirDao.insert(dirs[0])
                dirDao.insert(dirs[0])
                null
            } catch (e: Exception) {
                e
            }
        }.await()
        assert(
            e is SQLiteConstraintException,
            { "Expected exception to be thrown when saving same directory but got: $e" },
        )
    }

    @Test
    fun test_directories_are_correctly_mapped_to_smb_severs() = runTest {
        dirs.forEach { dirDao.insert(it) }
        smbs.forEach { smb ->
            dirDao.getSmbServerWithDirectories(smb.smbServerId).test {
                val lists = awaitItem()
                assert(lists.savedDirs.isNotEmpty())
                assertEquals(
                    "SMB '${smb.smbServerId}' has incorrect number of saved directories",
                    1,
                    lists.savedDirs.size,
                )
                cancel()
            }
        }
    }

    @Test
    fun test_directory_is_also_inserted_into_pending_sync_when_inserted() = runTest {
        dirs.forEach { dirDao.insertAndQueueForBackup(it) }
        smbs.forEach { smb ->
            assertEquals(
                "Only one directory should be pending for '${smb.smbServerId}' smb",
                1,
                dirDao.getPendingSyncDirectories(smb.smbServerId).size,
            )
        }
        val tempDirs: MutableList<DirectoryInfo> = dirs.toMutableList()
        tempDirs.removeIf { expDir ->
            val actualSyncDir = dirDao.getPendingSyncDirectories(expDir.smbServerId)[0]
            expDir.dirId == actualSyncDir.dirId &&
                    expDir.smbServerId == actualSyncDir.smbServerId &&
                    expDir.dirPath == actualSyncDir.dirPath
        }
        assertTrue(
            "These Directories should have been synced: '$tempDirs'",
            tempDirs.isEmpty(),
        )
    }

    @Test
    fun test_directories_are_also_inserted_into_pending_sync_when_inserted() = runTest {
        val smb = smbs.first()
        dirDao.getSmbServerWithDirectories(smb.smbServerId).test {
            val lists = awaitItem()
            assert(lists.savedDirs.isEmpty())
            cancel()
        }

        val expectedDirs = (1L..10).map { i ->
            DirectoryInfo(
                dirId = i,
                smbServerId = smb.smbServerId,
                dirPath = "some_uri_path_$i",
                dirName = "filename_$i.jpg",
                lastSynced = null,
            )
        }.toMutableList()

        expectedDirs.forEach { dirDao.insertAndQueueForBackup(it) }
        val actualSyncDirs = dirDao.getPendingSyncDirectories(smb.smbServerId)
        assertTrue(
            "Directories should have been inserted for sync",
            expectedDirs.size == actualSyncDirs.size,
        )
        expectedDirs.removeIf { expDir ->
            for (actualSyncDir in actualSyncDirs) {
                if (expDir.dirId == actualSyncDir.dirId &&
                    expDir.smbServerId == actualSyncDir.smbServerId &&
                    expDir.dirPath == actualSyncDir.dirPath
                ) {
                    return@removeIf true
                }
            }
            false
        }
        assertTrue(
            "These Directories should have been synced: '$expectedDirs'",
            expectedDirs.isEmpty(),
        )
    }

    @Test
    fun test_pending_sync_directories_are_removed_on_directory_removal() = runTest {
        val smb = smbs.first()
        val expectedDirs = (1L..10).map { i ->
            DirectoryInfo(
                dirId = i,
                smbServerId = smb.smbServerId,
                dirPath = "some_uri_path_$i",
                dirName = "filename_$i.jpg",
                lastSynced = null,
            )
        }.toMutableList()

        expectedDirs.forEach { dirDao.insertAndQueueForBackup(it) }
        var actualSyncDirs = dirDao.getPendingSyncDirectories(smb.smbServerId)
        assertTrue(
            "Directories should have been inserted for sync",
            expectedDirs.size == actualSyncDirs.size,
        )
        expectedDirs.forEach { dirDao.delete(it) }
        actualSyncDirs = dirDao.getPendingSyncDirectories(smb.smbServerId)
        assertTrue(
            "There should be no directories to sync",
            actualSyncDirs.isEmpty(),
        )
        expectedDirs.forEach {
            assertTrue(
                "This directory should have been removed",
                dirDao.getDirectoryById(it.dirId, smb.smbServerId) == null,
            )
        }
    }

    @Test
    fun test_directory_and_pending_sync_are_removed_on_smb_removal() = runTest {
        dirs.forEach { dirDao.insertAndQueueForBackup(it) }
        smbs.forEach { smbDao.delete(it) }
        dirs.forEach { dir ->
            assertTrue(
                "There should be no directories to sync",
                dirDao.getPendingSyncDirectories(dir.smbServerId).isEmpty(),
            )
            assertTrue(
                "There should be no directories to sync",
                dirDao.getDirectoryById(dir.dirId, dir.smbServerId) == null,
            )
        }
    }
}
