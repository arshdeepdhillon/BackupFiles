package com.ad.backupfiles

import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.ad.backupfiles.ui.utils.TestTag.Companion.EDIT_SMB_SERVER_FAB
import com.ad.backupfiles.ui.utils.TestTag.Companion.LAZY_COLUMN_TAG
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_CONNECT_BUTTON

/*
 * @author : Arshdeep Dhillon
 * @created : 30-Dec-23
*/
object TestHelper {

    /**
     * Retrieves a string from the provided resources using the specified string resource ID.
     *
     * @param resource The Resources object.
     * @param resourceId The resource ID of the string to retrieve.
     * @return The string value corresponding to the given resource ID.
     */
    fun getString(resource: Resources, @StringRes resourceId: Int): String {
        return resource.getString(resourceId)
    }

    /**
     * Gets the text of a Compose UI element with the specified tag.
     *
     * @param composeTestRule The Compose test rule for the activity.
     * @param tag The tag of the Compose UI element.
     * @param useUnmergedTree Flag to indicate whether to use an unmerged tree.
     * @return The text of the Compose UI element.
     */
    fun getText(
        composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>,
        tag: String,
        useUnmergedTree: Boolean = true,
    ): String {
        return composeTestRule.onNodeWithTag(tag, useUnmergedTree = useUnmergedTree).fetchSemanticsNode().config[SemanticsProperties.Text][0].text
    }

    /**
     * Navigates to the detail screen.
     *
     * @param composeTestRule The Compose test rule for the activity.
     */
    fun gotoDetailScreen(composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>) {
        composeTestRule.onNodeWithTag(LAZY_COLUMN_TAG).onChildren()[0].performTouchInput { click() }
    }

    /**
     * Navigates to the edit screen.
     *
     * @param composeTestRule The Compose test rule for the activity.
     */
    fun gotoEditScreen(composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>) {
        gotoDetailScreen(composeTestRule)
        composeTestRule.onNodeWithTag(EDIT_SMB_SERVER_FAB).performClick()
    }

    /**
     * Navigates to the shared content screen.
     *
     * @param composeTestRule The Compose test rule for the activity.
     */
    fun gotoSharedContentScreen(composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>) {
        gotoDetailScreen(composeTestRule)
        composeTestRule.onNodeWithTag(SMB_CONNECT_BUTTON).performClick()
    }
}
