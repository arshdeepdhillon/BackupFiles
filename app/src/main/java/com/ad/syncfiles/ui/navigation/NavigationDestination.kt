package com.ad.syncfiles.ui.navigation

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