package com.ad.backupfiles.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.ad.backupfiles.AppEntryPoint
import com.ad.backupfiles.BackupFilesApp
import com.ad.backupfiles.MockApplicationModuleImpl
import com.ad.backupfiles.TestHelper.gotoDetailScreen
import com.ad.backupfiles.TestHelper.gotoEditScreen
import com.ad.backupfiles.TestHelper.gotoSharedContentScreen
import com.ad.backupfiles.data.BackupFilesDatabase
import com.ad.backupfiles.data.dao.DirectoryDao
import com.ad.backupfiles.data.dao.SmbServerDao
import com.ad.backupfiles.data.entity.SmbServerInfo
import com.ad.backupfiles.smb.api.SMBClientApi
import com.ad.backupfiles.ui.addServerScreen.AddScreenDestination
import com.ad.backupfiles.ui.detailServerScreen.DetailScreenDestination
import com.ad.backupfiles.ui.editScreen.EditScreenDestination
import com.ad.backupfiles.ui.homeScreen.HomeDestination
import com.ad.backupfiles.ui.savedDirectoriesScreen.SavedDirectoriesScreenDestination
import com.ad.backupfiles.ui.theme.BackupFilesTheme
import com.ad.backupfiles.ui.utils.TestTag.Companion.LAZY_COLUMN_TAG
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_ADD_TAG
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_CONNECT_BUTTON
import com.ad.backupfiles.ui.utils.TestTag.Companion.TOP_APP_BAR_BACK_BUTTON
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

/*
 * @author : Arshdeep Dhillon
 * @created : 29-Dec-23
*/
@OptIn(ExperimentalCoroutinesApi::class)
class NavigationTest {

    private lateinit var context: Context
    private lateinit var backupDb: BackupFilesDatabase
    private lateinit var smbDao: SmbServerDao
    private lateinit var dirDao: DirectoryDao
    private lateinit var mockAppModule: MockApplicationModuleImpl
    private val testDispatcher = StandardTestDispatcher(TestCoroutineScheduler())
    private val testScope = TestScope(testDispatcher)
    private lateinit var navController: TestNavHostController

    @MockK
    private lateinit var mockSmbClientApi: SMBClientApi

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    companion object {
        private lateinit var device: UiDevice
        private const val TIMEOUT = 3000L

        @JvmStatic
        @BeforeClass
        fun setUp() {
            device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        }
    }

    private fun setupInMemoryDb() {
        backupDb = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BackupFilesDatabase::class.java).build()
        smbDao = backupDb.smbServerDao()
        dirDao = backupDb.directoryDao()
    }

    private fun setupDependencyInjection() {
        mockAppModule =
            MockApplicationModuleImpl(context = context, testScope = testScope, smbDao = smbDao, dirDao = dirDao, smbClient = mockSmbClientApi)
        AppEntryPoint.setAppModuleForTest(mockAppModule)
    }

    private fun setupAppNavHost() {
        composeTestRule.setContent {
            navController = TestNavHostController(context)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            BackupFilesTheme {
                BackupFilesApp(navController = navController)
            }
        }
    }

    private fun assertBackStackEntryOnHomeScreen() {
        assertEquals(HomeDestination.route, navController.currentBackStackEntry?.destination?.route)
    }

    private fun assertSmbDataVisible() {
        composeTestRule.waitUntil(TIMEOUT) { composeTestRule.onAllNodesWithTag(LAZY_COLUMN_TAG).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun assertDetailScreen() {
        assertEquals(DetailScreenDestination.routeArgs, navController.currentBackStackEntry?.destination?.route)
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        context = InstrumentationRegistry.getInstrumentation().targetContext

        setupInMemoryDb()
        setupDependencyInjection()
        setupAppNavHost()

        runTest {
            smbDao.upsert(
                SmbServerInfo(
                    smbServerId = 1L,
                    serverAddress = "192.168.100.10",
                    username = "test_username",
                    password = "test_password",
                    sharedFolderName = "test_share_folder",
                ),
            )
        }
    }

    @Test
    fun test_start_destination_screen() {
        assertBackStackEntryOnHomeScreen()
    }

    @Test
    fun test_from_start_to_add_smb_server_screen() {
        composeTestRule.onNodeWithTag(SMB_ADD_TAG).performClick()
        assertEquals(AddScreenDestination.route, navController.currentBackStackEntry?.destination?.route)

        device.pressBack()
        assertBackStackEntryOnHomeScreen()
    }

    @Test
    fun test_from_start_to_detail_screen() {
        assertSmbDataVisible()

        gotoDetailScreen(composeTestRule)
        assertDetailScreen()

        device.pressBack()
        assertBackStackEntryOnHomeScreen()
    }

    @Test
    fun test_from_start_to_edit_screen() {
        assertSmbDataVisible()

        gotoEditScreen(composeTestRule)
        assertEquals(EditScreenDestination.routeArgs, navController.currentBackStackEntry?.destination?.route)

        composeTestRule.onNodeWithTag(TOP_APP_BAR_BACK_BUTTON).performClick()
        assertDetailScreen()

        device.pressBack()
        assertBackStackEntryOnHomeScreen()
    }

    @Test
    fun test_from_start_to_shared_content_screen() {
        assertSmbDataVisible()

        gotoSharedContentScreen(composeTestRule)
        composeTestRule.onNodeWithTag(SMB_CONNECT_BUTTON).assertDoesNotExist()
        assertEquals(SavedDirectoriesScreenDestination.routeArgs, navController.currentBackStackEntry?.destination?.route)

        device.pressBack()
        assertDetailScreen()

        device.pressBack()
        assertBackStackEntryOnHomeScreen()

        gotoSharedContentScreen(composeTestRule)
        composeTestRule.onNodeWithTag(TOP_APP_BAR_BACK_BUTTON).performClick()
        composeTestRule.onNodeWithTag(TOP_APP_BAR_BACK_BUTTON).performClick()
        assertBackStackEntryOnHomeScreen()
    }
}
