package com.ad.backupfiles.ui.smbServer

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.ad.backupfiles.ui.theme.BackupFilesTheme
import com.ad.backupfiles.ui.utils.SmbServerData
import com.ad.backupfiles.ui.utils.TestTag.Companion.SHARED_FOLDER_DISPLAY_TEXT
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_CONNECT_BUTTON
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_DELETE_ALERT
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_DELETE_BUTTON
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_IP_DISPLAY_TEXT
import com.ad.backupfiles.ui.utils.TestTag.Companion.USERNAME_DISPLAY_TEXT
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

/*
 * @author : Arshdeep Dhillon
 * @created : 26-Nov-23
*/
@LargeTest
class DetailScreenTest {
    companion object {
        private lateinit var device: UiDevice

        @JvmStatic
        @BeforeClass
        fun setUp() {
            device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        }
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun test_components_remain_visible_on_long_input() {
        val uiState = DetailScreenUiState(
            SmbServerData(
                serverAddress = "1.1.1.1",
                username = "really_long_user_really_long_user_really_long_user_really_long_user",
                sharedFolderName = "really_long_shared_folder_name_really_long_shared_folder_name",
            ),
        )
        composeTestRule.setContent {
            BackupFilesTheme {
                DetailBody(uiState = uiState, handleDelete = {}, handleConnect = {})
            }
        }
        composeTestRule.onNodeWithTag(SMB_IP_DISPLAY_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(USERNAME_DISPLAY_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SHARED_FOLDER_DISPLAY_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SMB_DELETE_BUTTON).assert(isEnabled())
        composeTestRule.onNodeWithTag(SMB_CONNECT_BUTTON).assert(isEnabled())
    }

    @Test
    fun test_smb_delete_confirmation_prompt_is_presented() {
        val uiState = DetailScreenUiState(SmbServerData())
        val stateRestorationTester = StateRestorationTester(composeTestRule)

        stateRestorationTester.setContent {
            BackupFilesTheme {
                DetailBody(uiState = uiState, handleDelete = {}, handleConnect = {})
            }
        }
        composeTestRule.onNodeWithTag(SMB_DELETE_BUTTON).performClick()
        composeTestRule.onNodeWithTag(SMB_DELETE_ALERT).assertIsDisplayed()

        // Simulate a config change
        stateRestorationTester.emulateSavedInstanceStateRestore()
        composeTestRule.onNodeWithTag(SMB_DELETE_ALERT).assertIsDisplayed()
    }
}
