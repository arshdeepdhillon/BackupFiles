package com.ad.backupfiles.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ad.backupfiles.R
import com.ad.backupfiles.ui.shared.SmbServerInfoUiData
import com.ad.backupfiles.ui.theme.BackupFilesTheme

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Composable function to display an input form for server details.
 *
 * @param modifier Modifier for customizing the layout and appearance of the form.
 * @param smbServerData The server details to be displayed and edited in the form.
 * @param onFieldChange Callback function to handle changes in server detail fields.
 * @param enabled Flag to indicate whether the form fields should be enabled for user input.
 */
@Composable
fun InputForm(
    modifier: Modifier,
    smbServerData: SmbServerInfoUiData,
    onFieldChange: (SmbServerInfoUiData) -> Unit = {},
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.m_pad))
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = smbServerData.serverAddress,
            placeholder = { Text(stringResource(R.string.server_address_placeholder)) },
            onValueChange = { onFieldChange(smbServerData.copy(serverAddress = it)) },
            label = { Text(stringResource(R.string.server_url_req)) },
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
            value = smbServerData.username,
            onValueChange = { onFieldChange(smbServerData.copy(username = it)) },
            label = { Text(stringResource(R.string.username)) },
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
            value = smbServerData.password,
            onValueChange = { onFieldChange(smbServerData.copy(password = it)) },
            label = { Text(stringResource(R.string.password)) },
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
            value = smbServerData.sharedFolderName,
            placeholder = { Text(stringResource(R.string.shared_folder_placeholder)) },
            onValueChange = { onFieldChange(smbServerData.copy(sharedFolderName = it)) },
            label = { Text(stringResource(R.string.shared_folder_name_req)) },
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
                modifier = Modifier.padding(start = dimensionResource(id = R.dimen.m_pad))
            )
        }

    }

}

@Preview(showBackground = true)
@Composable
fun InputFormPreview() {
    BackupFilesTheme {
        InputForm(
            smbServerData = SmbServerInfoUiData(),
            onFieldChange = { },
            modifier = Modifier.fillMaxWidth()
        )
    }
}