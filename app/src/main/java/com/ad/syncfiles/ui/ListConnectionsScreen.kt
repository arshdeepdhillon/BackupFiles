package com.ad.syncfiles.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ad.syncfiles.R
import com.ad.syncfiles.data.SMBSaver
import com.ad.syncfiles.data.SMBUiState


/**
 * Shows list of shared connections or an informative text if [sharedDir] is empty.
 */
@Composable
fun ListConnectionsScreen(
    modifier: Modifier = Modifier,
    onAddSMBClicked: () -> Unit = {},
    onConnectClicked: () -> Unit = {},
    onEditClicked: (SMBUiState) -> Unit = {},
    sharedDir: List<SMBUiState> = emptyList(),
) {
    val EMPTY_SMB = SMBUiState()
    val selectedItem = rememberSaveable(stateSaver = SMBSaver) {
        mutableStateOf(SMBUiState())
    }
    val enableConnect = rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(R.dimen.md_pad))
            .padding(PaddingValues(top = dimensionResource(R.dimen.md_pad)))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onConnectClicked,
                enabled = enableConnect.value
            ) {
                Text(text = stringResource(R.string.label_connect))
            }
            Button(
                onClick = { onEditClicked(selectedItem.value) },
                enabled = enableConnect.value
            ) {
                Text(text = stringResource(R.string.label_edit))
            }
            Button(
                onClick = onAddSMBClicked,
            ) {
//                Icon(
//                    Icons.Filled.Add, contentDescription = "Add SMB details", modifier = Modifier.size(ButtonDefaults.IconSize)
//                )
                Text(text = stringResource(R.string.label_add_connection))
            }
        }
        // Display helpful text when there are no shared connections, otherwise display the connections.
        if (sharedDir.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = stringResource(R.string.info_no_smb_connections),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.divider_thickness))) {
                    itemsIndexed(sharedDir) { index, item ->
                        TextItem(label = item.serverUrl, selected = selectedItem.value == item) {
                            selectedItem.value =
                                if (selectedItem.value == item) {
                                    enableConnect.value = false
                                    EMPTY_SMB
                                } else {
                                    enableConnect.value = true
                                    item
                                }
                        }
                    }
                }
            }
        }
    }
}


/**
 * A single Card that displays [label].
 * @param label to display
 * @param selected  if true, highlights this [TextItem]
 * @param onClick invokes this callback when [TextItem] is clicked
 */
@Composable
fun TextItem(label: String, selected: Boolean, onClick: () -> Unit) {
    val selectedColor = if (selected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.surfaceVariant
    Card(
        modifier = Modifier.selectable(selected = selected, onClick = { onClick() }),
        colors = CardDefaults.cardColors(containerColor = selectedColor),
        shape = RoundedCornerShape(size = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListConnectionsScreenPreview() {
//    val d = List(5) { (1..99999).random() }.map {"$it \\\\192.192.198.28" }
    val d = List(5) { (1..99999).random() }.map { SMBUiState(serverUrl = "$it \\\\192.192.198.200", password = "", username = it.toString()) }
    ListConnectionsScreen(sharedDir = d)
}