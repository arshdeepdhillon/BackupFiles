package com.ad.syncfiles.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ad.syncfiles.ui.home.HomeDestination
import com.ad.syncfiles.ui.home.HomeScreen
import com.ad.syncfiles.ui.smbServer.SDEntryDestination
import com.ad.syncfiles.ui.smbServer.SDEntryScreen

/**
 * Navigation graph for the application
 */
@Composable
fun SyncFilesNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                navigateToItemEntry = { navController.navigate(SDEntryDestination.route) },
                navigateToItemUpdate = { navController.navigate("${SDEntryDestination.route}/${it}") }
            )
        }
        //TODO fix url now showing on main screen and these destinations
        composable(route = SDEntryDestination.route) {
            SDEntryScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}