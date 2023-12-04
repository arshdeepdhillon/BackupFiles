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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ad.backupfiles.R
import com.ad.backupfiles.ui.theme.BackupFilesTheme
import com.ad.backupfiles.ui.utils.TestTag.Companion.PASSWORD_INPUT_TEXT
import com.ad.backupfiles.ui.utils.TestTag.Companion.SHARED_FOLDER_INPUT_TEXT
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_IP_INPUT_TEXT
import com.ad.backupfiles.ui.utils.TestTag.Companion.USERNAME_INPUT_TEXT

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
 */
@Composable
fun InputForm(
    modifier: Modifier,
    smbServerData: SmbServerData,
    onFieldChange: (SmbServerData) -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.m_pad)),
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(SMB_IP_INPUT_TEXT),
            value = smbServerData.serverAddress,
            placeholder = { Text(stringResource(R.string.server_address_placeholder)) },
            onValueChange = { onFieldChange(smbServerData.copy(serverAddress = it)) },
            label = { Text(stringResource(R.string.server_url_req)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        )
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(USERNAME_INPUT_TEXT),
            value = smbServerData.username,
            onValueChange = { onFieldChange(smbServerData.copy(username = it)) },
            label = { Text(stringResource(R.string.username)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        )
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(PASSWORD_INPUT_TEXT),
            value = smbServerData.password,
            onValueChange = { onFieldChange(smbServerData.copy(password = it)) },
            label = { Text(stringResource(R.string.password)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        )
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(SHARED_FOLDER_INPUT_TEXT),
            value = smbServerData.sharedFolderName,
            placeholder = { Text(stringResource(R.string.shared_folder_placeholder)) },
            onValueChange = { onFieldChange(smbServerData.copy(sharedFolderName = it)) },
            label = { Text(stringResource(R.string.shared_folder_name_req)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        )
        Text(
            text = stringResource(R.string.required_fields),
            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.m_pad)),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InputFormPreview() {
    BackupFilesTheme {
        InputForm(
            smbServerData = SmbServerData(),
            onFieldChange = { },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
