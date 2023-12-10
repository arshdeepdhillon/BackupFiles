package com.ad.backupfiles.ui.smbServer

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ad.backupfiles.R
import com.ad.backupfiles.Toast
import com.ad.backupfiles.data.entity.SmbServerInfo
import com.ad.backupfiles.di.AppViewModelFactory
import com.ad.backupfiles.ui.navigation.NavigationDestination
import com.ad.backupfiles.ui.shared.InputForm
import com.ad.backupfiles.ui.shared.TopAppBar
import com.ad.backupfiles.ui.theme.BackupFilesTheme
import com.ad.backupfiles.ui.utils.SmbServerData
import com.ad.backupfiles.ui.utils.TestTag.Companion.EDIT_SCREEN_FOOTER
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
    viewModel: EditScreenViewModel = viewModel(factory = AppViewModelFactory.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(EditScreenDestination.titleRes),
                canNavBack = true,
                onNavUp = handleNavUp,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        EditScreenBody(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            uiState = viewModel.userInputState,
            isUiValid = uiState.isUiDataValid,
            onFieldChange = viewModel::updateUiState,
            handleSave = {
                coroutineScope.launch {
                    viewModel.updateItem()
                    handleNavBack()
                }
            },
            checkConnection = {
                coroutineScope.launch {
                    if (viewModel.canConnectToServer()) {
                        Toast.makeToast(context, R.string.connection_success_with_smb)
                    } else {
                        Toast.makeToast(context, R.string.connection_issue_with_smb)
                    }
                }
            },
        )
    }
}

/**
 * Composable function for displaying the body of an edit screen.
 *
 * @param modifier Modifier for customizing the layout of the EditScreenBody.
 * @param uiState The UI state representing server information.
 * @param isUiValid The UI validation state based on latest Ui state.
 * @param onFieldChange Callback function to handle changes in server details fields.
 * @param handleSave Callback function to handle the save button click.
 * @param checkConnection Callback function to handle the check connection button click.
 */
@Composable
fun EditScreenBody(
    modifier: Modifier = Modifier,
    uiState: SmbServerData,
    isUiValid: Boolean,
    onFieldChange: (SmbServerData) -> Unit,
    handleSave: () -> Unit,
    checkConnection: () -> Unit,
) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.content_layout_pad)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.m_pad)),
    ) {
        InputForm(
            modifier = Modifier.fillMaxWidth(),
            smbServerData = uiState,
            onFieldChange = onFieldChange,
        )
        EditScreenFooter(
            isDataValid = isUiValid,
            onSaveClick = handleSave,
            checkConnection = checkConnection,
        )
    }
}

/**
 * Composable function for rendering the footer section of an edit screen.
 *
 * @param isDataValid The current UI validation state.
 * @param onSaveClick Callback function invoked when the save button is pressed.
 * @param checkConnection Callback function invoked when the test connection button is pressed.
 */
@Composable
fun EditScreenFooter(
    isDataValid: Boolean,
    onSaveClick: () -> Unit,
    checkConnection: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            modifier = Modifier
                .testTag(EDIT_SCREEN_FOOTER)
                .testTag(""),
            onClick = checkConnection,
            enabled = isDataValid,
            shape = MaterialTheme.shapes.small,
        ) {
            Text(stringResource(R.string.test_connection))
        }
        Button(
            modifier = Modifier.testTag(EDIT_SCREEN_FOOTER),
            onClick = onSaveClick,
            enabled = isDataValid,
            shape = MaterialTheme.shapes.small,
        ) {
            Text(text = stringResource(id = R.string.save_smb))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditScreenPreviewUiInValid() {
    BackupFilesTheme {
        EditScreenBody(
            uiState = SmbServerData(),
            isUiValid = false,
            onFieldChange = {},
            handleSave = {},
            checkConnection = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditScreenPreviewUiValid() {
    BackupFilesTheme {
        EditScreenBody(
            uiState = SmbServerData(),
            isUiValid = true,
            onFieldChange = {},
            handleSave = {},
            checkConnection = {},
        )
    }
}
