package com.ad.backupfiles.ui.home

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ad.backupfiles.R
import com.ad.backupfiles.data.entity.SmbServerInfo
import com.ad.backupfiles.di.AppViewModelFactory
import com.ad.backupfiles.ui.navigation.NavigationDestination
import com.ad.backupfiles.ui.shared.TopAppBar
import com.ad.backupfiles.ui.theme.BackupFilesTheme
import com.ad.backupfiles.ui.utils.TestTag.Companion.LAZY_COLUMN_TAG
import com.ad.backupfiles.ui.utils.TestTag.Companion.NO_SAVED_SMBS_MSG_TAG
import com.ad.backupfiles.ui.utils.TestTag.Companion.SMB_ADD_TAG

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * A stateless singleton representing navigation details
 */
object HomeDestination : NavigationDestination {
    override val route = "Home"
    override val titleRes = R.string.app_name
}

/**
 * Composable function to display the saved SMB servers or an informative text.

 * @param modifier Modifier for customizing the layout of the HomeScreen.
 * @param handleFABClick Callback function to handle when user wants to create a new SMB server.
 * @param handleItemClick Callback function invoked when an item is clicked. SMBServerId of the clicked item is returned.
 * @param viewModel The [HomeViewModel] used to manage and retrieve data for the home screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    handleFABClick: () -> Unit,
    handleItemClick: (Long) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelFactory.Factory),
) {
    val homeUiState by viewModel.homeUiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { _ -> }
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = stringResource(id = HomeDestination.titleRes),
                canNavBack = false,
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = handleFABClick,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .testTag(SMB_ADD_TAG)
                    .padding(dimensionResource(id = R.dimen.m_pad)),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add_connection),
                )
            }
        },
    ) { innerPadding ->
        if (ContextCompat.checkSelfPermission(
                context,
                POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_DENIED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(POST_NOTIFICATIONS)
            }
        }
        HomeBody(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            servers = homeUiState.sharedServers,
            onItemClick = handleItemClick,
        )
    }
}

/**
 * Composable function to display the list of saved SMB items.
 *
 * @param modifier Modifier for customizing the layout and appearance of the HomeBody.
 * @param servers A list of SmbServerInfo objects to display.
 * @param onItemClick Callback function invoked when an item is clicked. SMBServerId of the clicked item is returned.
 */
@Composable
fun HomeBody(
    modifier: Modifier = Modifier,
    servers: List<SmbServerInfo>,
    onItemClick: (Long) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = dimensionResource(id = R.dimen.content_layout_pad)),
    ) {
        if (servers.isEmpty()) {
            Text(
                text = stringResource(R.string.message_on_empty_smbs),
                modifier = Modifier.testTag(NO_SAVED_SMBS_MSG_TAG),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall,
            )
        } else {
            ServerList(
                servers = servers,
                handleClick = { onItemClick(it.smbServerId) },
                modifier = Modifier,
            )
        }
    }
}

/**
 * Composable function to display a list of SMB servers.
 *
 * @param servers The list of SMB servers to display.
 * @param handleClick A callback function that handles item clicks when a server is selected. Returns the server object which was clicked.
 * @param modifier Modifier for customizing the layout and appearance of the ServerList.
 */
@Composable
fun ServerList(
    servers: List<SmbServerInfo>,
    handleClick: (SmbServerInfo) -> Unit,
    modifier: Modifier,
) {
    LazyColumn(modifier = modifier.testTag(LAZY_COLUMN_TAG)) {
        itemsIndexed(items = servers) { index, server ->
            ServerItem(
                server = server,
                modifier = Modifier
                    .padding(vertical = dimensionResource(id = R.dimen.s_pad))
                    .clickable { handleClick(server) },
            )
        }
    }
}

/**
 * Composable function to display information about a single SMB server.
 *
 * @param server A SMB server to be displayed.
 * @param modifier Modifier for customizing the layout and appearance of the ServerItem.
 */
@Composable
fun ServerItem(server: SmbServerInfo, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(size = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = server.serverAddress, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServerItemPreview() {
    BackupFilesTheme {
        ServerItem(
            SmbServerInfo(1, "192.168.10.10", "Test Username", "Test Password", "shared-dir"),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeBodyPreview() {
    BackupFilesTheme {
        HomeBody(
            servers = listOf(
                SmbServerInfo(1, "url1", "usr1", "pas1", "shared-folder"),
                SmbServerInfo(2, "url2", "usr2", "pas2", "shared-folder"),
            ),
            onItemClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeBodyEmptyListPreview() {
    BackupFilesTheme {
        HomeBody(
            servers = listOf(),
            onItemClick = {},
        )
    }
}
