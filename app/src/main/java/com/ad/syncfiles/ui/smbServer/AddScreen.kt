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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import kotlinx.coroutines.launch

object AddScreenDestination : NavigationDestination {
    override val route = "shared_device_entry"
    override val titleRes = R.string.shared_device_entry_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: AddScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            SyncFilesTopAppBar(
                title = stringResource(AddScreenDestination.titleRes),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        Body(
            uiState = viewModel.uiState,
            onSDValueChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.save()
                    navigateBack()
                }
            },
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        )
    }

}

@Composable
fun Body(uiState: UIState, onSDValueChange: (SharedDeviceDetails) -> Unit, onSaveClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.medium_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.medium_padding))
    ) {
        InputForm(sharedDirDetail = uiState.deviceDetails, onValueChange = onSDValueChange, modifier = Modifier.fillMaxWidth())
        Button(onClick = onSaveClick, enabled = uiState.isEntryValid, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(id = R.string.label_save_connection))
        }
    }
}

@Composable
fun InputForm(
    sharedDirDetail: SharedDeviceDetails,
    onValueChange: (SharedDeviceDetails) -> Unit = {},
    modifier: Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.medium_padding))
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = sharedDirDetail.serverUrl,
            onValueChange = { onValueChange(sharedDirDetail.copy(serverUrl = it)) },
            label = { Text(stringResource(R.string.label_server_url)) },
            singleLine = true,
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            )
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = sharedDirDetail.username,
            onValueChange = { onValueChange(sharedDirDetail.copy(username = it)) },
            label = { Text(stringResource(R.string.label_username)) },
            singleLine = true,
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            )
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = sharedDirDetail.password,
            onValueChange = { onValueChange(sharedDirDetail.copy(password = it)) },
            label = { Text(stringResource(R.string.label_password)) },
            singleLine = true,
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            )
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = sharedDirDetail.sharedFolderName,
            onValueChange = { onValueChange(sharedDirDetail.copy(sharedFolderName = it)) },
            label = { Text(stringResource(R.string.label_shared_folder_name)) },
            singleLine = true,
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            )
        )
        if (enabled) {
            Text(
                text = stringResource(R.string.required_fields),
                modifier = Modifier.padding(start = dimensionResource(id = R.dimen.medium_padding))
            )
        }

    }

}

@Preview(showBackground = true)
@Composable
fun AddScreenPreview() {
    SyncFilesTheme {
        Body(uiState = UIState(
            SharedDeviceDetails(
                serverUrl = "192.123.123.123",
                username = "Adding username",
                password = "Adding password",
                sharedFolderName = "SomeSharedFolderName"
            )
        ), onSDValueChange = {}, onSaveClick = { })
    }
}