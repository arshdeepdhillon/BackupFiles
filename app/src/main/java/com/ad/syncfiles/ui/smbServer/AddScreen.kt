package com.ad.syncfiles.ui.smbServer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ad.syncfiles.R
import com.ad.syncfiles.SyncFilesTopAppBar
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
object AddScreenDestination : NavigationDestination {
    override val route = "add_smb_server"
    override val titleRes = R.string.smb_server_add_title
}

/**
 * Composable function to display the Add Screen.
 *
 * @param handleNavBack Callback function to handle navigating back to the previous screen.
 * @param handleNavUp Callback function to handle navigating up within the screen hierarchy.
 * @param canNavBack Flag indicating whether the user can navigate back to the previous screen.
 * @param viewModel The [AddScreenViewModel] used for managing the data and logic of the Add Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    handleNavBack: () -> Unit,
    handleNavUp: () -> Unit,
    canNavBack: Boolean = true,
    viewModel: AddScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            SyncFilesTopAppBar(
                title = stringResource(AddScreenDestination.titleRes),
                canNavBack = canNavBack,
                onNavUp = handleNavUp
            )
        }
    ) { innerPadding ->
        AddScreenBody(
            uiState = viewModel.uiState,
            onFieldChange = viewModel::handleUiStateChange,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.save()
                    handleNavBack()
                }
            },
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        )
    }

}


/**
 * Composable function to display the body of an "Add Screen".
 *
 * @param uiState The UI state representing the latest server information.
 * @param onFieldChange Callback function to handle changes in the UI fields.
 * @param onSaveClick Callback function to handle the "Save" button click.
 * @param modifier Modifier for customizing the layout of the AddScreenBody.
 */
@Composable
fun AddScreenBody(
    uiState: ServerInfoUiState,
    onFieldChange: (ServerDetails) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.m_pad)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.m_pad))
    ) {
        InputForm(
            modifier = Modifier.fillMaxWidth(),
            sharedDirDetail = uiState.serverDetails,
            onFieldChange = onFieldChange
        )
        Button(
            onClick = onSaveClick,
            enabled = uiState.isValid,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.save_connection))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddScreenPreview() {
    SyncFilesTheme {
        AddScreenBody(
            uiState = ServerInfoUiState(
                ServerDetails(
                    serverAddress = "192.123.123.123",
                    username = "Adding username",
                    password = "Adding password",
                    sharedFolderName = "SomeSharedFolderName"
                )
            ),
            onFieldChange = {},
            onSaveClick = {}
        )
    }
}