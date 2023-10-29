package com.ad.syncfiles.ui.smbServer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ad.syncfiles.R
import com.ad.syncfiles.SyncFilesTopAppBar
import com.ad.syncfiles.Util
import com.ad.syncfiles.data.entity.SmbServerInfo
import com.ad.syncfiles.ui.AppViewModelProvider
import com.ad.syncfiles.ui.navigation.NavigationDestination
import com.ad.syncfiles.ui.theme.SyncFilesTheme
import com.ad.syncfiles.ui.utils.InputForm
import kotlinx.coroutines.launch

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */


/**
 * A stateless singleton representing navigation details
 */
object EditScreenDestination : NavigationDestination {
    override val route = "edit_smb_server"
    override val titleRes = R.string.smb_server_edit_title

    /**
     * Used for retrieving a specific [SmbServerInfo] to display
     */
    const val argKey = "smbServerArg"
    val routeArgs = "$route/{$argKey}"
}

/**
 * Composable that represents the screen for editing details of a server.
 *
 * @param handleNavBack Callback function to navigate back to the previous screen.
 * @param handleNavUp Callback function to navigate up within the screen.
 * @param modifier Modifier for customizing the layout of the EditScreen.
 * @param viewModel View model for managing the state and logic of the EditScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    handleNavBack: () -> Unit,
    handleNavUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    Scaffold(
        topBar = {
            SyncFilesTopAppBar(
                title = stringResource(EditScreenDestination.titleRes),
                canNavBack = true,
                onNavUp = handleNavUp
            )
        },
        modifier = modifier
    ) { innerPadding ->
        EditScreenBody(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            uiState = viewModel.uiState,
            onFieldChange = viewModel::updateUiState,
            handleSave = {
                coroutineScope.launch {
                    viewModel.updateItem()
                    handleNavBack()
                }
            },
            checkConnection = {
                coroutineScope.launch {
                    if (viewModel.testConnection()) {
                        Util.makeToast(context, R.string.connection_success_with_smb)
                    } else {
                        Util.makeToast(context, R.string.connection_issue_with_smb)
                    }
                }
            }
        )
    }
}

/**
 * Composable function for displaying the body of an edit screen.
 *
 * @param modifier Modifier for customizing the layout of the EditScreenBody.
 * @param uiState The UI state representing server information.
 * @param onFieldChange Callback function to handle changes in server details fields.
 * @param handleSave Callback function to handle the save button click.
 * @param checkConnection Callback function to handle the check connection button click.
 */
@Composable
fun EditScreenBody(
    modifier: Modifier = Modifier,
    uiState: ServerInfoUiState,
    onFieldChange: (ServerDetails) -> Unit,
    handleSave: () -> Unit,
    checkConnection: () -> Unit,
) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.content_layout_pad)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.m_pad))
    ) {
        InputForm(
            modifier = Modifier.fillMaxWidth(),
            sharedDirDetail = uiState.serverDetails,
            onFieldChange = onFieldChange
        )
        EditScreenFooter(
            uiState = uiState,
            onSaveClick = handleSave,
            checkConnection = checkConnection
        )
    }
}

/**
 * Composable function for rendering the footer section of an edit screen.
 *
 * @param uiState The UI state representing server information.
 * @param onSaveClick Callback function invoked when the save button is pressed.
 * @param checkConnection Callback function invoked when the test connection button is pressed.
 */
@Composable
fun EditScreenFooter(
    uiState: ServerInfoUiState,
    onSaveClick: () -> Unit,
    checkConnection: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = checkConnection,
            enabled = uiState.isValid,
            shape = MaterialTheme.shapes.small
        ) {
            Text(stringResource(R.string.Test))
        }
        Button(
            onClick = onSaveClick,
            enabled = uiState.isValid,
            shape = MaterialTheme.shapes.small
        ) {
            Text(text = stringResource(id = R.string.save_connection))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditScreenPreview() {
    SyncFilesTheme {
        EditScreen(handleNavBack = {}, handleNavUp = {})
    }
}
