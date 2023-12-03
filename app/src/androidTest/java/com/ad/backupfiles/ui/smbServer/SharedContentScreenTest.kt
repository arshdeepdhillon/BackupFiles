package com.ad.backupfiles.ui.smbServer

import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.ad.backupfiles.R
import com.ad.backupfiles.data.entity.DirectoryDto
import com.ad.backupfiles.ui.theme.BackupFilesTheme
import com.ad.backupfiles.ui.utils.SelectableItemsBody
import com.ad.backupfiles.ui.utils.TestTag.Companion.CHECK_BOX
import com.ad.backupfiles.ui.utils.TestTag.Companion.LAZY_COLUMN_TAG
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import java.time.Instant

/*
 * @author : Arshdeep Dhillon
 * @created : 28-Nov-23
*/
class SharedContentScreenTest {
    companion object {
        private lateinit var device: UiDevice
        private lateinit var savedDirs: List<DirectoryDto>
        private const val MAX_DIRS = 5

        @JvmStatic
        @BeforeClass
        fun setUpClass() {
            device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        }
    }

    @Before
    fun setup() {
        savedDirs = (0L until MAX_DIRS).map { i ->
            DirectoryDto(
                    dirId = i,
                    dirPath = "some_path_on_device_foldername_$i",
                    dirName = "foldername_$i",
                    smbServerId = i,
                    lastSynced = Instant.now().epochSecond
            )
        }.toList()
    }

    @get:Rule
    val composeTestRule = createComposeRule()


    /**
     * Verifies correct number of items are visible and there checked/unchecked icons are not visible.
     * @param descriptionOfCheckedBox The description of checked state of the checkbox.
     * @param descriptionOfUncheckedBox The description or identifier for the unchecked state of the checkbox.
     */
    private fun verifyDefaultState(descriptionOfCheckedBox: String, descriptionOfUncheckedBox: String) {
        composeTestRule.onNodeWithTag(LAZY_COLUMN_TAG).onChildren().assertCountEquals(MAX_DIRS)
        composeTestRule.onAllNodesWithTag(CHECK_BOX).assertAll(isNotEnabled())
        composeTestRule.onAllNodesWithContentDescription(descriptionOfUncheckedBox).assertAll(isNotEnabled())
        composeTestRule.onAllNodesWithContentDescription(descriptionOfCheckedBox).assertAll(isNotEnabled())
    }

    @Test
    fun test_components_visible_with_no_saved_directories() {
        composeTestRule.setContent {
            BackupFilesTheme {
                SelectableItemsBody(savedDirs = emptyList())
            }
        }
        composeTestRule.onNodeWithTag(LAZY_COLUMN_TAG).onChildren().assertCountEquals(0)
    }

    @Test
    fun test_components_visible_with_saved_directories() {
        composeTestRule.setContent {
            BackupFilesTheme {
                SelectableItemsBody(savedDirs = savedDirs)
            }
        }
        val descriptionOfCheckedBox = InstrumentationRegistry.getInstrumentation().targetContext.resources.getString(R.string.icon_checked)
        val descriptionOfUncheckedBox = InstrumentationRegistry.getInstrumentation().targetContext.resources.getString(R.string.icon_unchecked)
        verifyDefaultState(descriptionOfCheckedBox, descriptionOfUncheckedBox)
    }

    @Test
    fun test_long_press_on_one_directory() {
        val stateRestorationTester = StateRestorationTester(composeTestRule)
        stateRestorationTester.setContent {
            BackupFilesTheme {
                SelectableItemsBody(savedDirs = savedDirs)
            }
        }
        val descriptionOfCheckedBox = InstrumentationRegistry.getInstrumentation().targetContext.resources.getString(R.string.icon_checked)
        val descriptionOfUncheckedBox = InstrumentationRegistry.getInstrumentation().targetContext.resources.getString(R.string.icon_unchecked)
        verifyDefaultState(descriptionOfCheckedBox, descriptionOfUncheckedBox)

        // Long press on first item and verify the only on checkbox state is in checked state and rest are in unchecked state
        composeTestRule.onNodeWithTag(LAZY_COLUMN_TAG).onChildren()[0].performTouchInput { longClick() }
        stateRestorationTester.emulateSavedInstanceStateRestore()
        composeTestRule.onAllNodesWithContentDescription(descriptionOfUncheckedBox).assertCountEquals(MAX_DIRS - 1)
        composeTestRule.onAllNodesWithContentDescription(descriptionOfCheckedBox).assertCountEquals(1)

        // Long press on first checked item again and verify no checkbox is visible
        composeTestRule.onNodeWithTag(LAZY_COLUMN_TAG).onChildren()[0].performTouchInput { longClick() }
        composeTestRule.onAllNodesWithTag(CHECK_BOX).assertAll(isNotEnabled())

        // Verify recently unchecked checkbox doesn't reappear on recreation of composable.
        stateRestorationTester.emulateSavedInstanceStateRestore()
        composeTestRule.onAllNodesWithTag(CHECK_BOX).assertAll(isNotEnabled())
    }

    @Test
    fun test_long_press_on_all_directory() {

        val stateRestorationTester = StateRestorationTester(composeTestRule)
        stateRestorationTester.setContent {
            BackupFilesTheme {
                SelectableItemsBody(savedDirs = savedDirs)
            }
        }
        val descriptionOfCheckedBox = InstrumentationRegistry.getInstrumentation().targetContext.resources.getString(R.string.icon_checked)
        val descriptionOfUncheckedBox = InstrumentationRegistry.getInstrumentation().targetContext.resources.getString(R.string.icon_unchecked)
        verifyDefaultState(descriptionOfCheckedBox, descriptionOfUncheckedBox)

        // Long press on all items and simultaneously verify the correct state of checkboxes
        (1..MAX_DIRS).map { i ->
            composeTestRule.onNodeWithTag(LAZY_COLUMN_TAG).onChildren()[i - 1].performTouchInput { longClick() }
            // Verify we continue to have correct number of checked and unchecked boxes
            composeTestRule.onAllNodesWithContentDescription(descriptionOfCheckedBox).assertCountEquals(i)
            composeTestRule.onAllNodesWithContentDescription(descriptionOfUncheckedBox).assertCountEquals(MAX_DIRS - i)
        }

        // This is to verify all items remain checked on recreation of composable.
        stateRestorationTester.emulateSavedInstanceStateRestore()
        // Verify only checked checkboxes are visible
        composeTestRule.onNodeWithTag(LAZY_COLUMN_TAG).onChildren().assertCountEquals(MAX_DIRS)
        composeTestRule.onAllNodesWithTag(CHECK_BOX).assertAll(isEnabled()).assertCountEquals(MAX_DIRS)
        composeTestRule.onAllNodesWithContentDescription(descriptionOfCheckedBox).assertCountEquals(MAX_DIRS).assertAll(isEnabled())
        composeTestRule.onAllNodesWithContentDescription(descriptionOfUncheckedBox).assertCountEquals(0)

        // Now uncheck all items and verify no checkboxes are visible.
        // AKA: Verify the default state
        (1..MAX_DIRS).map { i ->
            composeTestRule.onNodeWithTag(LAZY_COLUMN_TAG).onChildren()[i - 1].performTouchInput { longClick() }
            // Verify we continue to have correct number of checked and unchecked boxes
            composeTestRule.onAllNodesWithContentDescription(descriptionOfCheckedBox).assertCountEquals(MAX_DIRS - i)

            if (i != MAX_DIRS) {
                composeTestRule.onAllNodesWithContentDescription(descriptionOfUncheckedBox).assertCountEquals(i)
            }

        }
        verifyDefaultState(descriptionOfCheckedBox, descriptionOfUncheckedBox)

    }

}