package com.ad.backupfiles.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ad.backupfiles.ui.addServerScreen.AddScreen
import com.ad.backupfiles.ui.addServerScreen.AddScreenDestination
import com.ad.backupfiles.ui.detailServerScreen.DetailScreen
import com.ad.backupfiles.ui.detailServerScreen.DetailScreenDestination
import com.ad.backupfiles.ui.editScreen.EditScreen
import com.ad.backupfiles.ui.editScreen.EditScreenDestination
import com.ad.backupfiles.ui.homeScreen.HomeDestination
import com.ad.backupfiles.ui.homeScreen.HomeScreen
import com.ad.backupfiles.ui.savedDirectoriesScreen.SavedDirectoriesScreen
import com.ad.backupfiles.ui.savedDirectoriesScreen.SavedDirectoriesScreenDestination

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Navigation graph for the application
 */
@Composable
fun BackupFilesNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier,
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                handleFABClick = { navController.navigate(AddScreenDestination.route) },
                handleItemClick = { navController.navigate("${DetailScreenDestination.route}/$it") },
            )
        }
        composable(route = AddScreenDestination.route) {
            AddScreen(
                handleNavBack = { navController.popBackStack() },
                handleNavUp = { navController.navigateUp() },
            )
        }
        composable(
            route = DetailScreenDestination.routeArgs,
            arguments = listOf(
                navArgument(DetailScreenDestination.argKey) { type = NavType.IntType },
            ),
        ) {
            DetailScreen(
                handleNavBack = { navController.popBackStack() },
                handleItemClicked = { navController.navigate("${EditScreenDestination.route}/$it") },
                handleConnect = { navController.navigate("${SavedDirectoriesScreenDestination.route}/$it") },
            )
        }
        composable(
            route = EditScreenDestination.routeArgs,
            arguments = listOf(
                navArgument(EditScreenDestination.argKey) { type = NavType.IntType },
            ),
        ) {
            EditScreen(
                handleNavBack = { navController.popBackStack() },
                handleNavUp = { navController.navigateUp() },
            )
        }
        composable(
            route = SavedDirectoriesScreenDestination.routeArgs,
            arguments = listOf(
                navArgument(SavedDirectoriesScreenDestination.argKey) {
                    type = NavType.LongType
                },
            ),
        ) {
            SavedDirectoriesScreen(
                handleNavUp = { navController.navigateUp() },
            )
        }
    }
}
