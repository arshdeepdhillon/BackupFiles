package com.ad.syncfiles.ui.home


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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ad.syncfiles.R
import com.ad.syncfiles.SyncFilesTopAppBar
import com.ad.syncfiles.data.ServerInfo
import com.ad.syncfiles.ui.AppViewModelProvider
import com.ad.syncfiles.ui.navigation.NavigationDestination
import com.ad.syncfiles.ui.theme.SyncFilesTheme

object HomeDestination : NavigationDestination {
    override val route = "Home"
    override val titleRes = R.string.app_name
}

/**
 * Shows list of shared connections or an informative text if [sharedDirs] is empty.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToItemEntry: () -> Unit,
    navigateToItemUpdate: (Int) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val homeUiState by viewModel.homeUiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()


    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SyncFilesTopAppBar(
                title = stringResource(id = HomeDestination.titleRes),
                canNavigateBack = false,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToItemEntry,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.medium_padding))
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.label_add_connection))
            }
        }
    ) { innerPadding ->
        HomeBody(
            serverList = homeUiState.sharedServerList,
            onItemClick = navigateToItemUpdate,
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }

}


@Composable
fun HomeBody(serverList: List<ServerInfo>, onItemClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (serverList.isEmpty()) {
            Text(
                text = stringResource(R.string.info_no_smb_connections),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall
            )
        } else {
            ServerList(
                itemList = serverList,
                onItemClick = { onItemClick(it.id) },
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.small_padding))
            )
        }
    }
}

@Composable
fun ServerList(itemList: List<ServerInfo>, onItemClick: (ServerInfo) -> Unit, modifier: Modifier) {
//    LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.divider_thickness))) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(items = itemList) { index, item ->
            TextItem(item = item, modifier = Modifier
                .padding(dimensionResource(id = R.dimen.small_padding))
                .clickable { onItemClick(item) })
        }
    }
}

/**
 * A single Card that displays [item].
 * @param item to display
 * @param selected  if true, highlights this [TextItem]
 * @param onClick invokes this callback when [TextItem] is clicked
 */
@Composable
fun TextItem(item: ServerInfo, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(size = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = item.serverUrl, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TextItemPreview() {
    SyncFilesTheme {
        TextItem(
            ServerInfo(1, "192.168.10.10", "Test Username", "Test Password", "shared-dir", System.currentTimeMillis())
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeBodyPreview() {
    SyncFilesTheme {
        HomeBody(
            listOf(ServerInfo(1, "url1", "usr1", "pas1"), ServerInfo(2, "url2", "usr2", "pas2")),
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeBodyEmptyListPreview() {
    SyncFilesTheme {
        HomeBody(
            listOf(),
            onItemClick = {}
        )
    }
}