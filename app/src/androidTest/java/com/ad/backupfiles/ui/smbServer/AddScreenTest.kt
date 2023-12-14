package com.ad.backupfiles.ui.smbServer

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.filters.LargeTest
import com.ad.backupfiles.ui.theme.BackupFilesTheme
import com.ad.backupfiles.ui.utils.SmbServerData
import com.ad.backupfiles.ui.utils.TestTag.Companion.PASSWORD_INPUT_TEXT
import com.ad.backupfiles.ui.utils.TestTag.Companion.SAVE_INPUT_FORM_BUTTON
import com.ad.backupfiles.ui.utils.TestTag.Companion.SHARED_FOLDER_INPUT_TEXT
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_IP_INPUT_TEXT
import com.ad.backupfiles.ui.utils.TestTag.Companion.USERNAME_INPUT_TEXT
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/*
 * @author : Arshdeep Dhillon
 * @created : 26-Nov-23
*/
@LargeTest
class AddScreenTest {
    private lateinit var uiState: SmbServerData

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        uiState = SmbServerData(
            serverAddress = "1.1.1.1",
            username = "someuser",
            password = "somepassword",
            sharedFolderName = "shared_folder",
        )
    }

    @Test
    fun test_default_components_visible_when_ui_is_invalid() {
        composeTestRule.setContent {
            BackupFilesTheme {
                AddScreenBody(
                    uiState = SmbServerData(),
                    isUiValid = false,
                    onFieldChange = {},
                    onSaveClick = {},
                )
            }
        }
        composeTestRule.onNodeWithTag(SMB_IP_INPUT_TEXT).assert(isEnabled())
        composeTestRule.onNodeWithTag(USERNAME_INPUT_TEXT).assert(isEnabled())
        composeTestRule.onNodeWithTag(PASSWORD_INPUT_TEXT).assert(isEnabled())
        composeTestRule.onNodeWithTag(SHARED_FOLDER_INPUT_TEXT).assert(isEnabled())
        composeTestRule.onNodeWithTag(SAVE_INPUT_FORM_BUTTON).assert(isNotEnabled())
    }

    @Test
    fun test_default_components_visible_when_ui_is_valid() {
        composeTestRule.setContent {
            BackupFilesTheme {
                AddScreenBody(
                    uiState = uiState,
                    isUiValid = true,
                    onFieldChange = {},
                    onSaveClick = {},
                )
            }
        }
        composeTestRule.onNodeWithTag(SMB_IP_INPUT_TEXT).assert(hasText(uiState.serverAddress))
        composeTestRule.onNodeWithTag(USERNAME_INPUT_TEXT).assert(hasText(uiState.username))
        composeTestRule.onNodeWithTag(PASSWORD_INPUT_TEXT).assert(hasText(uiState.password))
        composeTestRule.onNodeWithTag(SHARED_FOLDER_INPUT_TEXT)
            .assert(hasText(uiState.sharedFolderName))
        composeTestRule.onNodeWithTag(SAVE_INPUT_FORM_BUTTON).assert(isEnabled())
    }

    @Test
    fun test_ui_state_restoration_on_recreation() {
        val stateRestorationTester = StateRestorationTester(composeTestRule)
        stateRestorationTester.setContent {
            BackupFilesTheme {
                AddScreenBody(
                    uiState = uiState,
                    isUiValid = true,
                    onFieldChange = {},
                    onSaveClick = {},
                )
            }
        }
        // Simulate a recreation (ie: Configuration changes) to test restore of Ui state
        stateRestorationTester.emulateSavedInstanceStateRestore()

        composeTestRule.onNodeWithTag(SMB_IP_INPUT_TEXT).assert(hasText(uiState.serverAddress))
        composeTestRule.onNodeWithTag(USERNAME_INPUT_TEXT).assert(hasText(uiState.username))
        composeTestRule.onNodeWithTag(PASSWORD_INPUT_TEXT).assert(hasText(uiState.password))
        composeTestRule.onNodeWithTag(SHARED_FOLDER_INPUT_TEXT)
            .assert(hasText(uiState.sharedFolderName))
        composeTestRule.onNodeWithTag(SAVE_INPUT_FORM_BUTTON).assert(isEnabled())
    }
}
