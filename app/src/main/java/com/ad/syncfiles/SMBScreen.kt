package com.ad.syncfiles

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ad.syncfiles.data.DataSource
import com.ad.syncfiles.ui.ListConnectionsScreen
import com.ad.syncfiles.ui.SMBDetailScreen
import com.ad.syncfiles.ui.SMBViewModel
import com.ad.syncfiles.ui.SharedDirScreen


enum class SMBScreen { Main, AddSMBDetails, EditSMBDetails, DisplaySharedDir }

@Composable
fun SMBApp(
    viewModel: SMBViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val uiState by viewModel.uiState.collectAsState()
        NavHost(
            navController = navController,
            startDestination = SMBScreen.Main.name
        ) {
            composable(route = SMBScreen.Main.name) {
                ListConnectionsScreen(sharedDir = DataSource.d,
                    onConnectClicked = {
                        navController.navigate(SMBScreen.DisplaySharedDir.name)
                    }, onEditClicked = {
                        viewModel.setUrl(it.serverUrl)
                        viewModel.setUsername(it.username)
                        viewModel.setPassword(it.password)
                        navController.navigate(SMBScreen.EditSMBDetails.name)
                    },
                    onAddSMBClicked = {
                        /*TODO save uistate and launch */
                        navController.navigate(SMBScreen.AddSMBDetails.name)
                    })
            }
            composable(route = SMBScreen.AddSMBDetails.name) {
                SMBDetailScreen(uiState = uiState,
                    onSaveClicked = {
                        viewModel.setUrl(it.serverUrl)
                        viewModel.setUsername(it.username)
                        viewModel.setPassword(it.password)
                        navController.navigate(SMBScreen.Main.name)
                    }, onCancel = {
                        /* TODO  reset uiState then navigate*/
                        cancelAndGoToStart(viewModel, navController)
                    })
            }

            composable(route = SMBScreen.DisplaySharedDir.name) {
                SharedDirScreen()
            }
            composable(route = SMBScreen.EditSMBDetails.name) {
                SMBDetailScreen(uiState = uiState,
                    onSaveClicked = {
                        viewModel.setUrl(it.serverUrl)
                        viewModel.setUsername(it.username)
                        viewModel.setPassword(it.password)
                        navController.navigate(SMBScreen.Main.name)
                    }, onCancel = {
                        /* TODO  reset uiState then navigate*/
                        cancelAndGoToStart(viewModel, navController)
                    })
            }
        }
    }
}

private fun cancelAndGoToStart(viewModel: SMBViewModel, navController: NavHostController) {
    viewModel.resetSMB()
    // inclusive = false will remove everything from navController's back stack except for the given destination
    navController.popBackStack(SMBScreen.Main.name, inclusive = false)

}


@Preview
@Composable
fun SMBScreenPreview() {
    SMBApp()
}