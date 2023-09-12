package com.ad.syncfiles.ui.systemFolders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ad.syncfiles.R
import com.ad.syncfiles.SyncFilesTopAppBar
import com.ad.syncfiles.data.entity.SmbServerInfo
import com.ad.syncfiles.ui.AppViewModelProvider
import com.ad.syncfiles.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch
import java.io.File
import java.text.DateFormat
import java.util.Locale


/**
 * A stateless singleton representing navigation details
 */
object MediaBrowserScreenDestination : NavigationDestination {
    override val route = "display_system_files"
    override val titleRes = R.string.smb_server_gallery_title

    /**
     * Used for retrieving a specific [SmbServerInfo] to display
     */
    const val argKey = "folderPathArg"

    //    val routeArgs = "${route}?${argKey}={${argKey}}"
    val routeArgs = "${route}/{${argKey}}"
//    val routeArgs = "${route}/{${argKey}}"

}


/**
 * Allows the user to browse system files and select a folder to backup
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaBrowserScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    viewModel: MediaBrowserViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState = viewModel.uiState
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            SyncFilesTopAppBar(
                title = stringResource(MediaBrowserScreenDestination.titleRes),
                canNavigateBack = false
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        viewModel.saveSelectedBackupFolder()
                        navigateBack()
                    }
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.medium_padding))
            ) {
                Icon(imageVector = Icons.Default.Done, contentDescription = stringResource(id = R.string.add_connection))
            }
        },
        modifier = modifier
    ) { innerPadding ->
//        MediaBrowserBody(
//            modifier = Modifier.padding(innerPadding),
//            fileList = uiState.mediaFiles,
//            onItemClick = viewModel::onItemClick
//        )
        SystemFileProvider(modifier = Modifier.padding(innerPadding))

    }
}

@Composable
fun MediaBrowserBody(modifier: Modifier = Modifier, fileList: List<File>, onItemClick: (File) -> Unit = {}) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(1), modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.small_padding)),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(items = fileList) { index, item ->
            ItemDetails(
                modifier = Modifier.clickable { onItemClick(item) },
                itemName = item.name,
                modifiedTime = item.lastModified(),
                numOfFiles = item.listFiles()?.size,
                isDir = item.isDirectory
            )
        }
    }
}

@Composable
fun ItemDetails(itemName: String, modifier: Modifier = Modifier, modifiedTime: Long, numOfFiles: Int?, isDir: Boolean) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(size = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(id = R.dimen.small_padding)),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        painter = if (isDir) painterResource(id = R.drawable.folder_24) else painterResource(id = R.drawable.text_snippet_24),
                        contentDescription = "Content Type",
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.small_padding))
                    )
                    Column {
                        Text(text = itemName)
                        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = formatDate(modifiedTime),
                                style = MaterialTheme.typography.labelSmall
                            )
                            if (isDir) {
                                Text(
                                    text = "${
                                        when {
                                            numOfFiles == null -> 0
                                            numOfFiles <= 99 -> numOfFiles
                                            numOfFiles > 99 -> "99+"
                                            else -> 0
                                        }
                                    } items", style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


fun formatDate(timestampMillis: Long): String {
    val locale = Locale.getDefault()
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale).format(timestampMillis)
}


@Preview(showBackground = true)
@Composable
fun MediaBrowserBodyPreview() {
    MediaBrowserBody(
        fileList = listOf(
            File("temp1"),
            File("temp2"),
            File("temp3"),
        )
    )
}


@Composable
fun ThreeItemsCardLayout() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "one", modifier = Modifier.padding(dimensionResource(id = R.dimen.small_padding)))
                Column {
                    Text(text = "two")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "two_two")
                        Text(text = "three")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ThreeItemsCardPreview() {
    ThreeItemsCardLayout()
}
