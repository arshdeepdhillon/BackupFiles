package com.ad.backupfiles.ui.addServerScreen

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ad.backupfiles.R
import com.ad.backupfiles.di.ApplicationViewModelFactory
import com.ad.backupfiles.ui.navigation.NavigationDestination
import com.ad.backupfiles.ui.shared.InputForm
import com.ad.backupfiles.ui.shared.TopAppBar
import com.ad.backupfiles.ui.theme.BackupFilesTheme
import com.ad.backupfiles.ui.utils.SmbServerData
import com.ad.backupfiles.ui.utils.TestTag.Companion.SAVE_INPUT_FORM_BUTTON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
 * @param viewModel The [AddServerViewModel] used for managing the data and logic of the Add Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    handleNavBack: () -> Unit,
    handleNavUp: () -> Unit,
    canNavBack: Boolean = true,
    viewModel: AddServerViewModel = viewModel(factory = ApplicationViewModelFactory.Factory),
) {
    val viewState by viewModel.viewState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(AddScreenDestination.titleRes),
                canNavBack = canNavBack,
                onNavUp = handleNavUp,
            )
        },
    ) { innerPadding ->
        AddScreenBody(
            uiState = viewModel.userInputState,
            isUiValid = viewState.isValid,
            onFieldChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.save()
                    withContext(Dispatchers.Main) { handleNavBack() }
                }
            },
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
        )
    }
}

/**
 * Composable function to display the body of an "Add Screen".
 *
 * @param uiState The UI state representing the latest server information.
 * @param isUiValid The UI validation state based on latest Ui state.
 * @param onFieldChange Callback function to handle changes in the UI fields.
 * @param onSaveClick Callback function to handle the "Save" button click.
 * @param modifier Modifier for customizing the layout of the AddScreenBody.
 */
@Composable
fun AddScreenBody(
    uiState: SmbServerData,
    isUiValid: Boolean,
    onFieldChange: (SmbServerData) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.m_pad)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.m_pad)),
    ) {
        InputForm(
            modifier = Modifier.fillMaxWidth(),
            smbServerData = uiState,
            onFieldChange = onFieldChange,
        )
        Button(
            onClick = onSaveClick,
            enabled = isUiValid,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(SAVE_INPUT_FORM_BUTTON),
        ) {
            Text(text = stringResource(id = R.string.save_smb))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddScreenPreview() {
    BackupFilesTheme {
        AddScreenBody(
            uiState = SmbServerData(),
            isUiValid = false,
            onFieldChange = {},
            onSaveClick = {},
        )
    }
}
