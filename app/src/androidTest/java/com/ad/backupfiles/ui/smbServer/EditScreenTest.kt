package com.ad.backupfiles.ui.smbServer

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.ad.backupfiles.R
import com.ad.backupfiles.ui.theme.BackupFilesTheme
import com.ad.backupfiles.ui.utils.SmbServerData
import com.ad.backupfiles.ui.utils.TestTag.Companion.EDIT_SCREEN_FOOTER
import com.ad.backupfiles.ui.utils.TestTag.Companion.PASSWORD_INPUT_TEXT
import com.ad.backupfiles.ui.utils.TestTag.Companion.SHARED_FOLDER_INPUT_TEXT
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_IP_INPUT_TEXT
import com.ad.backupfiles.ui.utils.TestTag.Companion.USERNAME_INPUT_TEXT
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

/*
 * @author : Arshdeep Dhillon
 * @created : 27-Nov-23
*/
class EditScreenTest {
    companion object {
        private lateinit var device: UiDevice
        private lateinit var uiState: SmbServerData

        @JvmStatic
        @BeforeClass
        fun setUpClass() {
            device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        }
    }

    @Before
    fun setup() {
        uiState = SmbServerData(serverAddress = "1.1.1.1", username = "someuser", password = "somepassword", sharedFolderName = "shared_folder")
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun test_footer_content_enabled_on_valid_data() {
        composeTestRule.setContent {
            BackupFilesTheme {
                EditScreenBody(
                    uiState = uiState,
                    isUiValid = true,
                    onFieldChange = {},
                    handleSave = {},
                    checkConnection = {},
                )
            }
        }
        composeTestRule.onNodeWithTag(SMB_IP_INPUT_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(USERNAME_INPUT_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SHARED_FOLDER_INPUT_TEXT).assertIsDisplayed()
        composeTestRule.onAllNodesWithTag(EDIT_SCREEN_FOOTER).assertAll(isEnabled())
    }

    @Test
    fun test_footer_content_disabled_on_invalid_data() {
        composeTestRule.setContent {
            BackupFilesTheme {
                EditScreenBody(
                    uiState = uiState,
                    isUiValid = false,
                    onFieldChange = {},
                    handleSave = {},
                    checkConnection = {},
                )
            }
        }
        composeTestRule.onNodeWithTag(SMB_IP_INPUT_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(USERNAME_INPUT_TEXT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SHARED_FOLDER_INPUT_TEXT).assertIsDisplayed()
        composeTestRule.onAllNodesWithTag(EDIT_SCREEN_FOOTER).assertAll(isNotEnabled())
    }

    @Test
    fun test_ui_state_restore_on_recreation() {
        val serverAddress = "1.1.1.1"
        val username = "someuser"
        val password = "somepassword"
        val sharedFolderName = "shared_folder"
        var savePressed = false
        var testConnectionPressed = false
        val stateRestorationTester = StateRestorationTester(composeTestRule)
        stateRestorationTester.setContent {
            BackupFilesTheme {
                EditScreenBody(
                    uiState = uiState,
                    isUiValid = true,
                    onFieldChange = {},
                    handleSave = { savePressed = true },
                    checkConnection = { testConnectionPressed = true },
                )
            }
        }
        composeTestRule.onAllNodesWithTag(EDIT_SCREEN_FOOTER).assertAll(isEnabled())
        composeTestRule.onNodeWithTag(SMB_IP_INPUT_TEXT).assert(hasText(serverAddress))
        composeTestRule.onNodeWithTag(USERNAME_INPUT_TEXT).assert(hasText(username))
        composeTestRule.onNodeWithTag(PASSWORD_INPUT_TEXT).assert(hasText(password))
        composeTestRule.onNodeWithTag(SHARED_FOLDER_INPUT_TEXT).assert(hasText(sharedFolderName))

        // Simulate a config change
        stateRestorationTester.emulateSavedInstanceStateRestore()

        composeTestRule.onNodeWithTag(SMB_IP_INPUT_TEXT).assert(hasText(serverAddress))
        composeTestRule.onNodeWithTag(USERNAME_INPUT_TEXT).assert(hasText(username))
        composeTestRule.onNodeWithTag(PASSWORD_INPUT_TEXT).assert(hasText(password))
        composeTestRule.onNodeWithTag(SHARED_FOLDER_INPUT_TEXT).assert(hasText(sharedFolderName))
        device.findObject(
            By.text(InstrumentationRegistry.getInstrumentation().targetContext.resources.getString(R.string.test_connection)),
        ).click()
        assert(testConnectionPressed, { "Expected the 'Test' connection button to produce and event" })

        device.findObject(
            By.text(InstrumentationRegistry.getInstrumentation().targetContext.resources.getString(R.string.add_smb)),
        ).click()
        assert(savePressed, { "Expected the 'Save' button to produce and event" })
    }
}
