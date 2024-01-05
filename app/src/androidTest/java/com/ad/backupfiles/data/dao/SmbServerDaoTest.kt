package com.ad.backupfiles.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import com.ad.backupfiles.TestDispatcherRule
import com.ad.backupfiles.data.BackupFilesDatabase
import com.ad.backupfiles.data.entity.SmbServerInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
class SmbServerDaoTest {

    private lateinit var backupDb: BackupFilesDatabase
    private lateinit var smbDao: SmbServerDao
    private val testDispatcher = StandardTestDispatcher(TestCoroutineScheduler())

    @get: Rule
    val dispatcherRule = TestDispatcherRule(testDispatcher)

    @Before
    fun setup() {
        // Setup in-memory database for testing
        backupDb = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BackupFilesDatabase::class.java).build()
        smbDao = backupDb.smbServerDao()

        runTest {
            smbs.forEach { smbDao.upsert(it) }
        }
    }

    companion object {
        private lateinit var smbs: MutableList<SmbServerInfo>

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
            }.toMutableList()
        }
    }

    @Test
    fun test_smb_update() = runTest {
        smbs.forEachIndexed { i, smb ->
            smbs[i] = smb.copy(sharedFolderName = "new_shared_folder")
                .copy(serverAddress = "123.456.789.000")
        }
        smbs.forEach { smbDao.upsert(it) }
        smbs.forEach { smb ->
            assertEquals(
                smb,
                smbDao.getById(smb.smbServerId),
            )
        }
    }
}
