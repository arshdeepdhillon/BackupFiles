package com.ad.syncfiles.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ad.syncfiles.ui.home.HomeDestination
import com.ad.syncfiles.ui.home.HomeScreen
import com.ad.syncfiles.ui.smbServer.AddScreen
import com.ad.syncfiles.ui.smbServer.AddScreenDestination
import com.ad.syncfiles.ui.smbServer.DetailScreen
import com.ad.syncfiles.ui.smbServer.DetailScreenDestination
import com.ad.syncfiles.ui.smbServer.EditScreen
import com.ad.syncfiles.ui.smbServer.EditScreenDestination
import com.ad.syncfiles.ui.smbServer.SharedContentScreen
import com.ad.syncfiles.ui.smbServer.SharedContentScreenDestination
import com.ad.syncfiles.ui.systemFolders.MediaBrowserScreen
import com.ad.syncfiles.ui.systemFolders.MediaBrowserScreenDestination

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
                navigateToItemEntry = { navController.navigate(AddScreenDestination.route) },
                navigateToItemUpdate = { navController.navigate("${DetailScreenDestination.route}/${it}") }
            )
        }
        composable(route = AddScreenDestination.route) {
            AddScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(route = DetailScreenDestination.routeArgs,
            arguments = listOf(
                navArgument(DetailScreenDestination.argKey) { type = NavType.IntType }
            )
        ) {
            DetailScreen(
                navigateBack = { navController.popBackStack() },
                navigateToEditItem = { navController.navigate("${EditScreenDestination.route}/$it") },
                navigateToSharedContent = { navController.navigate("${SharedContentScreenDestination.route}/$it") }
            )
        }
        composable(route = EditScreenDestination.routeArgs,
            arguments = listOf(
                navArgument(EditScreenDestination.argKey) { type = NavType.IntType }
            )
        ) {
            EditScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(
            route = SharedContentScreenDestination.routeArgs,
            arguments = listOf(navArgument(SharedContentScreenDestination.argKey) { type = NavType.IntType })
        ) {
            SharedContentScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() },
                onAddSyncFolder = { navController.navigate("${MediaBrowserScreenDestination.route}/$it") }
            )
        }
        composable(route = MediaBrowserScreenDestination.routeArgs,
            arguments = listOf(
                navArgument(MediaBrowserScreenDestination.argKey) { type = NavType.IntType }
            )
        ) {
            MediaBrowserScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
    }
}