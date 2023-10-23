package com.ad.syncfiles.ui.smbServer

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ad.syncfiles.R
import com.ad.syncfiles.SyncFilesTopAppBar
import com.ad.syncfiles.data.entity.SmbServerInfo
import com.ad.syncfiles.ui.AppViewModelProvider
import com.ad.syncfiles.ui.navigation.NavigationDestination
import com.ad.syncfiles.ui.theme.SyncFilesTheme
import kotlinx.coroutines.launch

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */


/**
 * A stateless singleton representing navigation details
 */
object DetailScreenDestination : NavigationDestination {
    override val route = "details_smb_server"
    override val titleRes = R.string.smb_server_details_title

    /**
     * Used for retrieving a specific [SmbServerInfo] to display
     */
    const val argKey = "smbServerArg"
    val routeArgs = "$route/{$argKey}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    handleNavBack: () -> Unit,
    handleItemClicked: (Int) -> Unit,
    handleConnect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState = viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            SyncFilesTopAppBar(
                title = stringResource(DetailScreenDestination.titleRes),
                canNavBack = true,
                onNavUp = handleNavBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { handleItemClicked(uiState.value.deviceDetails.id) },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.medium_padding))
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.smb_server_edit_title)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        DetailBody(
            uiState = uiState.value,
            handleDelete = {
                coroutineScope.launch {
                    viewModel.deleteSmbServer()
                    handleNavBack()
                }
            },
            handleConnect = { handleConnect(uiState.value.deviceDetails.id) },
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        )
    }
}

@Composable
fun DetailBody(
    uiState: DetailUIState,
    handleDelete: () -> Unit,
    modifier: Modifier = Modifier,
    handleConnect: () -> Unit
) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.small_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.medium_padding)),
    ) {
        var isDeleteDialogActive by rememberSaveable { mutableStateOf(false) }
        ServerDetails(
            item = uiState.deviceDetails.toSmbServerInfo(),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isDeleteDialogActive = true },
                shape = MaterialTheme.shapes.small
            ) {
                Text(stringResource(R.string.delete))
            }
            Button(
                onClick = handleConnect,
                shape = MaterialTheme.shapes.small
            ) {
                Text(stringResource(id = R.string.connect))
            }
        }
        if (isDeleteDialogActive) {
            DeleteAlert(
                handleAccept = {
                    isDeleteDialogActive = false
                    handleDelete()
                },
                handleCancel = { isDeleteDialogActive = false },
                modifier = Modifier.padding(dimensionResource(id = R.dimen.medium_padding))
            )
        }
    }
}

/**
 * Composable function to display details of a server.
 *
 * @param item The server information to display.
 * @param modifier Modifier for customizing the layout.
 */
@Composable
fun ServerDetails(item: SmbServerInfo, modifier: Modifier) {
    Card(
        modifier = modifier, colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = modifier.padding(dimensionResource(id = R.dimen.small_padding)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.small_padding))
        ) {
            DetailRow(
                labelResID = R.string.server_url,
                itemDetail = item.serverAddress,
                modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.small_padding)
                )
            )
            DetailRow(
                labelResID = R.string.username,
                itemDetail = item.username,
                modifier = Modifier.padding(
                    horizontal = dimensionResource(id = R.dimen.small_padding)
                )
            )
            DetailRow(
                labelResID = R.string.shared_folder_name,
                itemDetail = item.sharedFolderName,
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.small_padding))
            )

        }
    }
}

/**
 * Composable function to display a single detail row within the [ServerDetails].
 *
 * @param labelResID The [StringRes] for the label.
 * @param itemDetail The specific detail information to be displayed.
 * @param modifier Modifier for customizing the layout.
 */
@Composable
private fun DetailRow(
    @StringRes labelResID: Int, itemDetail: String, modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Text(text = stringResource(labelResID))
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = itemDetail,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Composable function to display a delete confirmation alert dialog.
 *
 * @param handleAccept Callback function to handle the user's acceptance of the delete action.
 * @param handleCancel Callback function to handle the user's cancellation of the delete action.
 * @param modifier Modifier for customizing the layout.
 */
@Composable
fun DeleteAlert(handleAccept: () -> Unit, handleCancel: () -> Unit, modifier: Modifier) {
    AlertDialog(onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.attention)) },
        text = { Text(stringResource(R.string.delete_question)) },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = handleCancel) {
                Text(text = stringResource(R.string.no))
            }
        },
        confirmButton = {
            TextButton(onClick = handleAccept) {
                Text(text = stringResource(R.string.yes))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview() {
    SyncFilesTheme {
        DetailBody(uiState = DetailUIState(
            deviceDetails = ServerDetails(
                serverAddress = "192.123.123.123",
                username = "Editing name",
                password = "Editing pass",
                sharedFolderName = "Editing really long shared directory name"
            )
        ), handleDelete = { }, handleConnect = {})
    }
}

@Preview
@Composable
fun DeleteConfirmationDialogPreview() {
    SyncFilesTheme {
        DeleteAlert(handleAccept = { }, handleCancel = { }, modifier = Modifier)
    }
}