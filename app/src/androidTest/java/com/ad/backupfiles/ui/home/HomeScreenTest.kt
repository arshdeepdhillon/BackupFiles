package com.ad.backupfiles.ui.home

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import com.ad.backupfiles.data.entity.SmbServerInfo
import com.ad.backupfiles.ui.theme.BackupFilesTheme
import com.ad.backupfiles.ui.utils.TestTag.Companion.LAZY_COLUMN_TAG
import com.ad.backupfiles.ui.utils.TestTag.Companion.NO_SAVED_SMBS_MSG_TAG
import org.junit.Rule
import org.junit.Test

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Nov-23
*/

/**
 * Instrumented test, which will execute on an Android device.
 */

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun test_default_components_visible_when_zero_smbs_saved() {
        // Launch the screen
        composeTestRule.setContent {
            BackupFilesTheme {
                HomeBody(servers = emptyList(), onItemClick = {})
            }
        }
        composeTestRule.onNodeWithTag(NO_SAVED_SMBS_MSG_TAG).assertIsDisplayed()
    }

    @Test
    fun test_default_components_visible_when_smbs_saved() {
        val smbs = (1..5).map { i ->
            SmbServerInfo(
                serverAddress = "192.100.10.$i",
                username = "testname$i",
                password = "testpass$i",
                sharedFolderName = "testfolder$i",
            )
        }.toList()
        composeTestRule.setContent {
            BackupFilesTheme {
                HomeBody(
                    servers = smbs,
                    onItemClick = {},
                )
            }
        }
        composeTestRule.onNodeWithTag(NO_SAVED_SMBS_MSG_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(LAZY_COLUMN_TAG).onChildren().assertCountEquals(5)
        composeTestRule.onNodeWithTag(LAZY_COLUMN_TAG).onChildren().onFirst().assert(hasText(smbs[0].serverAddress))
    }
}
