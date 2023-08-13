package com.ad.syncfiles.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ad.syncfiles.R

@Composable
fun EmptyMainScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier
        .fillMaxSize()
        .padding(dimensionResource(R.dimen.md_pad))) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { /* ... */ },
                // Uses ButtonDefaults.ContentPadding by default
//                contentPadding = PaddingValues(start = 10.dp, top = 6.dp, end = 10.dp, bottom = 6.dp)
            ) {
                Icon(
                    Icons.Filled.Add, contentDescription = "Add SMB details", modifier = Modifier.size(ButtonDefaults.IconSize)
                )
            }

        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = stringResource(id = R.string.info_no_smb_connections),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }

}

@Preview
@Composable
fun EmptyMainScreenPreview() {
    EmptyMainScreen()
}