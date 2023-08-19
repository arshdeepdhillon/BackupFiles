package com.ad.syncfiles.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ad.syncfiles.R
import com.ad.syncfiles.data.SMBUiState


/**
 *
 */
@Composable
fun SMBDetailScreen(modifier: Modifier = Modifier, uiState: SMBUiState, onSaveClicked: (SMBUiState) -> Unit = {}, onCancel: () -> Unit = {}) {

    var serverUrl by rememberSaveable { mutableStateOf(uiState.serverUrl) }
    var username by rememberSaveable { mutableStateOf(uiState.username) }
    var password by rememberSaveable { mutableStateOf(uiState.password) }
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.md_pad))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                /*TODO reset then cancel*/
                onClick = onCancel,
                // Uses ButtonDefaults.ContentPadding by default
                contentPadding = PaddingValues(start = 10.dp, top = 6.dp, end = 10.dp, bottom = 6.dp)
            ) {
                Text(stringResource(R.string.label_cancel_connection))
            }
            Button(
                /*TODO update using uiState viewmodel */
                onClick = { onSaveClicked(SMBUiState(serverUrl = serverUrl, username = username, password = password)) },
                // Uses ButtonDefaults.ContentPadding by default
                contentPadding = PaddingValues(start = 10.dp, top = 6.dp, end = 10.dp, bottom = 6.dp)
            ) {
                Text(stringResource(R.string.label_save_connection))
            }
        }
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.divider_thickness)))
        Column(
            modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.md_pad))
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text(stringResource(R.string.label_server_url)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(R.string.label_username)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.label_password)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SMBServerDetailScreenPreview() {
    val tmp = SMBUiState()
    SMBDetailScreen(uiState = tmp)
}
