package com.ad.syncfiles.ui.smbServer

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ad.syncfiles.R
import com.ad.syncfiles.SyncFilesTopAppBar
import com.ad.syncfiles.data.entity.SmbServerInfo
import com.ad.syncfiles.ui.AppViewModelProvider
import com.ad.syncfiles.ui.navigation.NavigationDestination
import com.ad.syncfiles.ui.theme.SyncFilesTheme


/**
 * A stateless singleton representing navigation details
 */
object SharedContentScreenDestination : NavigationDestination {
    override val route = "shared_content_smb_server"
    override val titleRes = R.string.smb_server_gallery_title

    /**
     * Used for retrieving a specific [SmbServerInfo] to display
     */
    const val argKey = "smbServerArg"
    val routeArgs = "$route/{$argKey}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedContentScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SharedContentScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onAddSyncFolder: (Int) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            SyncFilesTopAppBar(
                title = stringResource(SharedContentScreenDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAddSyncFolder(viewModel.uiState.deviceDetails.id)
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.medium_padding))
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.add_connection))
            }
        },
        modifier = modifier
    ) { innerPadding ->
        /*TODO*/
        Text("T44ODO", modifier = modifier.padding(innerPadding))
    }
}


@Preview(showBackground = true)
@Composable
fun SharedContentScreenPreview() {
    SyncFilesTheme {
        SharedContentScreen(
            navigateBack = {},
            onNavigateUp = {},
            onAddSyncFolder = {})
    }
}

