package com.ad.backupfiles.worker

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ListenableWorker.Result
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.workDataOf
import com.ad.backupfiles.AppEntryPoint
import com.ad.backupfiles.MockApplicationModuleImpl
import com.ad.backupfiles.TestDispatcherRule
import com.ad.backupfiles.data.BackupFilesDatabase
import com.ad.backupfiles.data.dao.DirectoryDao
import com.ad.backupfiles.data.dao.SmbServerDao
import com.ad.backupfiles.data.entity.DirectoryInfo
import com.ad.backupfiles.data.entity.DirectorySyncInfo
import com.ad.backupfiles.data.entity.SmbServerInfo
import com.ad.backupfiles.smb.api.SMBClientApi
import com.ad.backupfiles.worker.UploadFolderWorkerConstants.SMB_ID_KEY
import com.ad.backupfiles.worker.UploadFolderWorkerConstants.WORKER_TYPE_TAG
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.lang.Thread.sleep
import java.nio.file.Path
import kotlin.io.path.createTempFile

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
@LargeTest
class UploadFolderWorkerTest {

    private val SMB_ID: Long = 1
    private val tempFiles: ArrayList<Path> = arrayListOf()
    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler, name = "UploadFolderWorkerTD")
    private val testScope = TestScope(testDispatcher)
    private lateinit var minimumRequiredData: Data
    private lateinit var smb: SmbServerInfo
    private lateinit var dirs: ArrayList<DirectoryInfo>
    private lateinit var context: Context
    private lateinit var workerUnderTest: TestListenableWorkerBuilder<UploadFolderWorker>
    private lateinit var workManager: WorkManager
    private lateinit var configuration: Configuration
    private lateinit var backupDb: BackupFilesDatabase
    private lateinit var smbDao: SmbServerDao
    private lateinit var dirDao: DirectoryDao
    private lateinit var mockAppModule: MockApplicationModuleImpl

    @get: Rule
    val dispatcherRule = TestDispatcherRule(testDispatcher)

    @MockK
    private lateinit var mockSmbClientApi: SMBClientApi

    private fun createTempData() {
        for (i in 0..2) {
            tempFiles.add(createTempFile(prefix = "testFile", suffix = "txt"))
        }
    }

    private fun setupWorker() {
        // Configure WorkManager, change the log level to make it easier to debug and use a SynchronousExecutor to make it easier to write tests.
        configuration = Configuration.Builder().setMinimumLoggingLevel(Log.VERBOSE).setExecutor(SynchronousExecutor()).build()
        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, configuration)
        workManager = WorkManager.getInstance(context)
        // Setup the test ListenableWorker.
        workerUnderTest = TestListenableWorkerBuilder(context = context)
        createTempData()
    }

    private fun setupInMemoryDb() {
        backupDb = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BackupFilesDatabase::class.java).build()
        smbDao = backupDb.smbServerDao()
        dirDao = backupDb.directoryDao()
    }

    private fun setupDependencyInjection() {
        mockAppModule = MockApplicationModuleImpl(context = context, testScope = testScope, smbDao = smbDao, dirDao = dirDao, smbClient = mockSmbClientApi)
        AppEntryPoint.setAppModuleForTest(mockAppModule)
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        setupInMemoryDb()
        setupDependencyInjection()
        setupWorker()

        smb = SmbServerInfo(
            smbServerId = SMB_ID,
            serverAddress = "192.168.100.10",
            username = "test_username",
            password = "test_password",
            sharedFolderName = "share_folder",
        )
        dirs = tempFiles.mapIndexed { i, path ->
            DirectoryInfo(
                dirId = i.toLong() + 1,
                smbServerId = smb.smbServerId,
                dirPath = path.toUri().toString(),
                dirName = path.fileName.toString(),
                lastSynced = null,
            )
        }.toCollection(ArrayList())
        minimumRequiredData = workDataOf(SMB_ID_KEY to SMB_ID, WORKER_TYPE_TAG to WorkerType.SYNC.name)
    }

    @Test
    fun test_worker_fails_on_incorrect_work_data() = runTest {
        val missingSmbDataWorker = workerUnderTest.setInputData(workDataOf(WORKER_TYPE_TAG to WorkerType.SYNC.name)).build()
        var workResult = missingSmbDataWorker.doWork()
        assertEquals(Result.Failure(), workResult)

        val missingWorkerTypeDataWorker = workerUnderTest.setInputData(workDataOf(SMB_ID_KEY to SMB_ID)).build()
        workResult = missingWorkerTypeDataWorker.doWork()
        assertEquals(Result.Failure(), workResult)
    }

    @Test
    fun test_worker_begins_when_connected_to_network() {
        // Setup test driver that allows changing constraints and delays
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val request = OneTimeWorkRequestBuilder<UploadFolderWorker>().setConstraints(constraints).setInputData(minimumRequiredData).build()

        // Block the current thread until the enqueue of request has completed
        workManager.enqueue(request).result.get()

        // Request should be enqueued when (there is no network connection) constraint is not met
        var workerInfo = workManager.getWorkInfoById(request.id).get()
        assertEquals(WorkInfo.State.ENQUEUED, workerInfo.state)

        with(testDriver) { setAllConstraintsMet(request.id) }
        workerInfo = workManager.getWorkInfoById(request.id).get()
        // Request should now be running
        assertEquals(WorkInfo.State.RUNNING, workerInfo.state)
    }

    @Test
    fun test_lastSyncTime_is_updated_after_uploading() = runTest {
        coEvery { mockSmbClientApi.saveFolder(any(), any(), any(), any()) } returns Unit

        val worker = workerUnderTest.setInputData(minimumRequiredData).build()
        smbDao.upsert(smb)
        dirs.forEach { dirDao.insertAndQueueForBackup(it) }

        // Start the work synchronously and test the result
        assertEquals(Result.Success(), worker.startWork().get())
        advanceUntilIdle()
        dirs.forEach { dirInfo ->
            val syncedDir = dirDao.getDirectoryById(dirInfo.dirId, dirInfo.smbServerId)!!
            assertNotNull("Not synced: $syncedDir", syncedDir.lastSynced)
        }
    }

    @Test
    fun test_lastSyncTime_is_updated_on_resync() = runTest {
        coEvery { mockSmbClientApi.saveFolder(any(), any(), any(), any()) } returns Unit
        var worker = workerUnderTest.setInputData(minimumRequiredData).build()

        smbDao.upsert(smb)
        dirs.forEach { dirDao.insertAndQueueForBackup(it) }

        assertEquals(Result.Success(), worker.startWork().get())
        advanceUntilIdle()

        val initialSyncDirs = arrayListOf<DirectoryInfo>()
        dirs.forEach { dirInfo ->
            val syncedDir = dirDao.getDirectoryById(dirInfo.dirId, dirInfo.smbServerId)!!
            initialSyncDirs.add(syncedDir)
        }
        dirs.map { dirInfo -> DirectorySyncInfo(dirId = dirInfo.dirId, smbServerId = dirInfo.smbServerId, dirPath = dirInfo.dirPath) }
            .forEach { dirDao.insertDirectoryForSync(it) }

        // Need at least 1 second of delay so initialSyncDirs's lastSynced is different enough between two work requests
        sleep(2000)
        worker = workerUnderTest.setInputData(minimumRequiredData).build()
        assertEquals(Result.Success(), worker.startWork().get())
        advanceUntilIdle()

        initialSyncDirs.forEach { initialSyncDir ->
            val reSyncedDir = dirDao.getDirectoryById(initialSyncDir.dirId, initialSyncDir.smbServerId)!!
            assert(reSyncedDir.lastSynced!! > initialSyncDir.lastSynced!!)
        }
    }

    @Test
    fun test_worker_result_when_work_is_cancelled() = runTest {
        coEvery { mockSmbClientApi.saveFolder(any(), any(), any(), any()) } returns Unit
        val request = OneTimeWorkRequestBuilder<UploadFolderWorker>()
            .setInputData(minimumRequiredData)
            .build()

        smbDao.upsert(smb)
        while (tempFiles.size <= 100) {
            tempFiles.add(createTempFile(prefix = "testFile", suffix = "txt"))
        }
        dirs = tempFiles.mapIndexed { i, path ->
            DirectoryInfo(
                dirId = i.toLong() + 1,
                smbServerId = smb.smbServerId,
                dirPath = path.toUri().toString(),
                dirName = path.fileName.toString(),
                lastSynced = null,
            )
        }.toCollection(ArrayList())
        dirs.forEach { dirDao.insertAndQueueForBackup(it) }

        workManager.enqueue(request).result.get()
        sleep(400L) // Delay the cancel request to allow some work to be done
        workManager.cancelWorkById(request.id)

        sleep(400L) // Give some time for cancel to propagate
        assertEquals(WorkInfo.State.CANCELLED, workManager.getWorkInfoById(request.id).get().state)

        advanceUntilIdle()
        assert(dirDao.getPendingSyncDirectories(smb.smbServerId).isEmpty())
    }
}
