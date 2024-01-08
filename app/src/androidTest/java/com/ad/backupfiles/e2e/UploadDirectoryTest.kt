package com.ad.backupfiles.e2e

import android.content.Context
import android.content.res.Resources
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.ad.backupfiles.AppEntryPoint
import com.ad.backupfiles.BackupFilesApp
import com.ad.backupfiles.MainActivity
import com.ad.backupfiles.MockApplicationModuleImpl
import com.ad.backupfiles.R
import com.ad.backupfiles.TestHelper.getString
import com.ad.backupfiles.TestHelper.getText
import com.ad.backupfiles.data.BackupFilesDatabase
import com.ad.backupfiles.data.dao.DirectoryDao
import com.ad.backupfiles.data.dao.SmbServerDao
import com.ad.backupfiles.data.entity.DirectoryInfo
import com.ad.backupfiles.smb.api.SMBClientApi
import com.ad.backupfiles.ui.savedDirectoriesScreen.SavedDirectoriesScreenDestination
import com.ad.backupfiles.ui.theme.BackupFilesTheme
import com.ad.backupfiles.ui.utils.SmbServerData
import com.ad.backupfiles.ui.utils.TestTag.Companion.LAZY_COLUMN_TAG
import com.ad.backupfiles.ui.utils.TestTag.Companion.PASSWORD_INPUT_TEXT
import com.ad.backupfiles.ui.utils.TestTag.Companion.PICK_DIR_TAG
import com.ad.backupfiles.ui.utils.TestTag.Companion.SAVE_INPUT_FORM_BUTTON
import com.ad.backupfiles.ui.utils.TestTag.Companion.SHARED_FOLDER_INPUT_TEXT
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_ADD_TAG
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_CONNECT_BUTTON
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_IP_INPUT_TEXT
import com.ad.backupfiles.ui.utils.TestTag.Companion.SYNC_STATUS_TITLE
import com.ad.backupfiles.ui.utils.TestTag.Companion.USERNAME_INPUT_TEXT
import com.ad.backupfiles.worker.WorkerType
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import java.util.regex.Pattern

/*
 * @author : Arshdeep Dhillon
 * @created : 29-Dec-23
*/
@OptIn(ExperimentalCoroutinesApi::class)
@LargeTest
class UploadDirectoryTest {
    // "sdk_" text was retrieved from root view of Directory picker
    private val rootDirPathPattern: Pattern = Pattern.compile("sdk_.*", Pattern.CASE_INSENSITIVE)
    private val testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
    private val testScope = TestScope(testDispatcher)
    private lateinit var context: Context
    private lateinit var resource: Resources
    private lateinit var mockAppModule: MockApplicationModuleImpl
    private lateinit var navController: TestNavHostController
    private lateinit var workManager: WorkManager

    @MockK
    private lateinit var mockSmbClientApi: SMBClientApi

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    companion object {
        private const val TIMEOUT = 3000L
        private lateinit var smbData: SmbServerData
        private lateinit var device: UiDevice
        private lateinit var smbDao: SmbServerDao
        private lateinit var dirDao: DirectoryDao
        private lateinit var backupDb: BackupFilesDatabase

        @JvmStatic
        @BeforeClass
        fun setUp() {
            device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            setupInMemoryDb()

            smbData = SmbServerData(
                serverAddress = "1.1.1.1",
                username = "someuser",
                password = "somepassword",
                sharedFolderName = "shared_folder",
            )
        }

        /**
         * Same database will be used for all tests
         */
        private fun setupInMemoryDb() {
            backupDb = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BackupFilesDatabase::class.java).build()
            smbDao = backupDb.smbServerDao()
            dirDao = backupDb.directoryDao()
        }
    }

    private fun setupDependencyInjection() {
        mockAppModule = MockApplicationModuleImpl(context = context, testScope = testScope, smbDao = smbDao, dirDao = dirDao, smbClient = mockSmbClientApi)
        AppEntryPoint.setAppModuleForTest(mockAppModule)
        workManager = WorkManager.getInstance(context)
    }

    private fun setupAppNavHost() {
        composeTestRule.activity.setContent {
            navController = TestNavHostController(context)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            BackupFilesTheme {
                BackupFilesApp(navController = navController)
            }
        }
    }

    /**
     * Creates one smb server and fills in appropriate fields using the given [uiState]
     * @param smbData used to fill in appropriate fields
     */
    private fun createSmbServer(smbData: SmbServerData) {
        composeTestRule.onNodeWithTag(SMB_ADD_TAG).performClick()
        composeTestRule.onNodeWithTag(SMB_IP_INPUT_TEXT).performTextInput(smbData.serverAddress)
        composeTestRule.onNodeWithTag(USERNAME_INPUT_TEXT).performTextInput(smbData.username)
        composeTestRule.onNodeWithTag(PASSWORD_INPUT_TEXT).performTextInput(smbData.password)
        composeTestRule.onNodeWithTag(SHARED_FOLDER_INPUT_TEXT).performTextInput(smbData.sharedFolderName)
        composeTestRule.onNodeWithTag(SAVE_INPUT_FORM_BUTTON).performClick()
    }

    /**
     * Navigates from HomeScreen to SavedDirectoriesScreen.
     * **Note:** Assumes there is only one smb server is saved, so first item is clicked.
     */
    private fun gotoSavedDirsScreen() {
        composeTestRule.onNodeWithTag(LAZY_COLUMN_TAG).onChildren()[0].performTouchInput { click() }
        composeTestRule.onNodeWithTag(SMB_CONNECT_BUTTON).performClick()
    }

    /**
     * Clicks an UI element with text matching the patterns "USE THIS FOLDER" and "ALLOW".
     */
    private fun backupCurrentFolder() {
        val useThisFolderTxtPattern = Pattern.compile("USE THIS FOLDER", Pattern.CASE_INSENSITIVE)
        val allowTxtPattern = Pattern.compile("ALLOW", Pattern.CASE_INSENSITIVE)
        device.findObject(By.text(useThisFolderTxtPattern)).clickAndWait(Until.newWindow(), TIMEOUT)
        device.findObject(By.text(allowTxtPattern)).clickAndWait(Until.newWindow(), TIMEOUT)
    }

    /**
     * Navigates to the specified folder paths.
     *
     * @param dirPaths List of folder paths to navigate through.
     */
    private fun navigateToFolder(dirPaths: List<String>) {
        device.wait(Until.hasObject(By.text(rootDirPathPattern)), TIMEOUT)
        device.findObject(By.text(rootDirPathPattern)).clickAndWait(Until.newWindow(), TIMEOUT)

        val dirPatterns = dirPaths.map { Pattern.compile(it, Pattern.CASE_INSENSITIVE) }
        dirPatterns.forEach { dirNamePattern: Pattern -> device.findObject(By.text(dirNamePattern)).clickAndWait(Until.newWindow(), TIMEOUT) }
    }

    /** Verifies the following when re-saving an already saved directory:
     * 1. SyncQueue DB was empty
     * 2. Only 1 Worker was created when the directory was backed for first time. Re-saving should not result it 2 Workers.
     *
     * @param dirPath directory to re-save
     */
    private fun assertDirectoryIsNotResaved(dirPath: List<String>) {
        composeTestRule.onNodeWithTag(PICK_DIR_TAG).performClick()
        navigateToFolder(dirPath)
        backupCurrentFolder()

        composeTestRule.waitUntil(TIMEOUT) {
            composeTestRule
                .onAllNodesWithText(getString(resource, SavedDirectoriesScreenDestination.titleRes))
                .fetchSemanticsNodes().size == 1
        }
        composeTestRule.onNodeWithTag(LAZY_COLUMN_TAG).onChildren().assertCountEquals(1)

        runTest {
            val smb = smbDao.getByAddedDate().first().first()
            val dirs: List<DirectoryInfo> = dirDao.getSmbServerWithDirectories(smb.smbServerId).first().savedDirs
            assertEquals(1, dirs.size)
            assertEquals(0, dirDao.getPendingSyncDirectories(smb.smbServerId).size)

            // There should only every be 1 worker created when we first uploaded the directory
            val workerInfo = workManager.getWorkInfosByTag(WorkerType.BACKUP.name).get()
            assertEquals(1, workerInfo.size)
            assertEquals(WorkInfo.State.SUCCEEDED, workerInfo[0].state)
        }
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        resource = composeTestRule.activity.resources
        setupDependencyInjection()
        setupAppNavHost()
    }

    @Test
    fun test_create_smb_server_and_upload_a_directory() {
        val expectedSyncValue = getString(resource, R.string.sync_status_title) + getString(resource, R.string.not_yet_sync_status)
        val dirPath = listOf("Android", "Media")

        createSmbServer(smbData)
        gotoSavedDirsScreen()

        // Open the Directory picker and backup a directory
        composeTestRule.onNodeWithTag(PICK_DIR_TAG).performClick()
        navigateToFolder(dirPath)
        backupCurrentFolder()

        // Give some time for screen to be displayed when coming from Directory picker activity
        composeTestRule.waitUntil(TIMEOUT) {
            composeTestRule
                .onAllNodesWithText(getString(resource, SavedDirectoriesScreenDestination.titleRes))
                .fetchSemanticsNodes().size == 1
        }
        composeTestRule.waitUntil(TIMEOUT) {
            composeTestRule.onAllNodesWithTag(LAZY_COLUMN_TAG).fetchSemanticsNodes().size == 1
        }

        // Verify the sync title has a timestamp, which indicates the Worker uploaded the directory successfully
        composeTestRule.onNodeWithTag(LAZY_COLUMN_TAG).onChildren().assertCountEquals(1)
        composeTestRule.waitUntil(TIMEOUT) { getText(composeTestRule, SYNC_STATUS_TITLE) != expectedSyncValue }

        assertDirectoryIsNotResaved(dirPath)
    }
}
