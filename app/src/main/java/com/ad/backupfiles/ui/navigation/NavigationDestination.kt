package com.ad.backupfiles.ui.navigation

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Describe the navigation destinations for the application
 */
interface NavigationDestination {
    /**
     * **Unique** name to define the path for a composable
     */
    val route: String

    /**
     * String resource id of the title which to be displayed in the screen.
     */
    val titleRes: Int
}
