package com.ad.backupfiles.ui.detailServerScreen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ad.backupfiles.R
import com.ad.backupfiles.data.entity.SmbServerInfo
import com.ad.backupfiles.di.ApplicationViewModelFactory
import com.ad.backupfiles.ui.navigation.NavigationDestination
import com.ad.backupfiles.ui.shared.GeneralAlert
import com.ad.backupfiles.ui.shared.TopAppBar
import com.ad.backupfiles.ui.theme.BackupFilesTheme
import com.ad.backupfiles.ui.utils.SmbServerData
import com.ad.backupfiles.ui.utils.TestTag.Companion.EDIT_SMB_SERVER_FAB
import com.ad.backupfiles.ui.utils.TestTag.Companion.SHARED_FOLDER_DISPLAY_TEXT
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_CONNECT_BUTTON
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_DELETE_ALERT
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_DELETE_BUTTON
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_IP_DISPLAY_TEXT
import com.ad.backupfiles.ui.utils.TestTag.Companion.USERNAME_DISPLAY_TEXT
import com.ad.backupfiles.ui.utils.toSmbServerEntity
import kotlinx.coroutines.launch

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * A stateless singleton representing navigation details
 */
object DetailScreenDestination : NavigationDestination {
    override val route = "details_smb_server"
    override val titleRes = R.string.smb_server_details_title

    /**
     * Used for retrieving a specific [SmbServerInfo] to display
     */
    const val argKey = "smbServerArg"
    val routeArgs = "$route/{$argKey}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    handleNavBack: () -> Unit,
    handleItemClicked: (Long) -> Unit,
    handleConnect: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailServerViewModel = viewModel(factory = ApplicationViewModelFactory.Factory),
) {
    val coroutineScope = rememberCoroutineScope()
    val viewState = viewModel.viewState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(DetailScreenDestination.titleRes),
                canNavBack = true,
                onNavUp = handleNavBack,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { handleItemClicked(viewState.value.serverInfo.id) },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.m_pad))
                    .testTag(EDIT_SMB_SERVER_FAB),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.smb_server_edit_title),
                )
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        DetailBody(
            uiState = viewState.value,
            handleDelete = {
                coroutineScope.launch {
                    viewModel.deleteSmbServer()
                    handleNavBack()
                }
            },
            handleConnect = { handleConnect(viewState.value.serverInfo.id) },
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        )
    }
}

@Composable
fun DetailBody(
    uiState: DetailScreenUiState,
    handleDelete: () -> Unit,
    modifier: Modifier = Modifier,
    handleConnect: () -> Unit,
) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.content_layout_pad)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.m_pad)),
    ) {
        var isDeleteDialogActive by rememberSaveable { mutableStateOf(false) }
        ServerDetailForm(
            item = uiState.serverInfo.toSmbServerEntity(),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = { isDeleteDialogActive = true },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.testTag(SMB_DELETE_BUTTON),
            ) {
                Text(stringResource(R.string.delete))
            }
            Button(
                onClick = handleConnect,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.testTag(SMB_CONNECT_BUTTON),
            ) {
                Text(stringResource(id = R.string.connect))
            }
        }
        if (isDeleteDialogActive) {
            GeneralAlert(
                handleAccept = {
                    isDeleteDialogActive = false
                    handleDelete()
                },
                titleId = R.string.confirm_title_alert,
                bodyId = R.string.delete_body_alert,
                handleCancel = { isDeleteDialogActive = false },
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.m_pad))
                    .testTag(SMB_DELETE_ALERT),
            )
        }
    }
}

/**
 * Composable function to display details of a server.
 *
 * @param item The server information to display.
 * @param modifier Modifier for customizing the layout.
 */
@Composable
fun ServerDetailForm(item: SmbServerInfo, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(
            modifier = modifier.padding(dimensionResource(id = R.dimen.s_pad)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.s_pad)),
        ) {
            DetailRow(
                labelResID = R.string.server_url,
                itemDetail = item.serverAddress,
                modifier = Modifier
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.s_pad),
                    )
                    .testTag(SMB_IP_DISPLAY_TEXT),
            )
            DetailRow(
                labelResID = R.string.username,
                itemDetail = item.username,
                modifier = Modifier
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.s_pad),
                    )
                    .testTag(USERNAME_DISPLAY_TEXT),
            )
            DetailRow(
                labelResID = R.string.shared_folder_name,
                itemDetail = item.sharedFolderName,
                modifier = Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.s_pad))
                    .testTag(SHARED_FOLDER_DISPLAY_TEXT),
            )
        }
    }
}

/**
 * Composable function to display a single detail row within the [ServerDetailForm].
 *
 * @param labelResID The [StringRes] for the label.
 * @param itemDetail The specific detail information to be displayed.
 * @param modifier Modifier for customizing the layout.
 */
@Composable
private fun DetailRow(
    @StringRes labelResID: Int,
    itemDetail: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Text(text = stringResource(labelResID))
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = itemDetail,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview() {
    BackupFilesTheme {
        DetailBody(
            uiState = DetailScreenUiState(
                serverInfo = SmbServerData(
                    serverAddress = "192.123.123.123",
                    username = "Editing name",
                    password = "Editing pass",
                    sharedFolderName = "Editing really long shared directory name",
                ),
            ),
            handleDelete = { },
            handleConnect = {},
        )
    }
}
