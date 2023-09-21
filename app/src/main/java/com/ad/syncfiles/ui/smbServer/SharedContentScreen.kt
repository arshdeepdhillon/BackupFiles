package com.ad.syncfiles.ui.smbServer

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ad.syncfiles.R
import com.ad.syncfiles.SyncFilesTopAppBar
import com.ad.syncfiles.data.entity.SmbServerInfo
import com.ad.syncfiles.ui.AppViewModelProvider
import com.ad.syncfiles.ui.lists.ItemListBody
import com.ad.syncfiles.ui.navigation.NavigationDestination
import com.ad.syncfiles.ui.theme.SyncFilesTheme
import kotlinx.coroutines.launch


/**
 * A stateless singleton representing navigation details
 */
object SharedContentScreenDestination : NavigationDestination {
    override val route = "shared_content_smb_server"
    override val titleRes = R.string.smb_server_saved_title

    /**
     * Used for retrieving a specific [SmbServerInfo] to display
     */
    const val argKey = "smbServerArg"
    val routeArgs = "$route/{$argKey}"
}

/*
 TODO:
    * When a directory is selected for backup, show that directory in UI instead of its contents.
    * After a directory is selected for backup, start saving its content to the SMB server.
    * When directory is being saved, disallow clicks on it.
    * Allow touch events on that directory, once directory is fully saved.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedContentScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SharedContentScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedDirUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val dirPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { contentUri ->
        selectedDirUri = contentUri
        coroutineScope.launch {
            contentUri?.let {
                viewModel.addBackupDirInfo(contentUri.toString())
            }
        }
    }

    Scaffold(
        topBar = {
            SyncFilesTopAppBar(
                title = stringResource(SharedContentScreenDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    dirPickerLauncher.launch(null)
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(dimensionResource(id = R.dimen.medium_padding))
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.add_connection))
            }
        },
        modifier = modifier
    ) { innerPadding ->
        ItemListBody(modifier = Modifier.padding(innerPadding), fileList = getDocument(context, uiState.content))
    }
}


fun getDocument(context: Context, uriPaths: List<String>): List<DocumentFile> {
    // TODO fix uriPaths
    if (uriPaths.isEmpty()) {
        return emptyList()
    }
    return DocumentFile.fromTreeUri(context, Uri.parse(uriPaths.get(0)))?.listFiles()?.toList() ?: emptyList()
}

@Preview(showBackground = true)
@Composable
fun SharedContentScreenPreview() {
    SyncFilesTheme {
        SharedContentScreen(onNavigateUp = {})
    }
}

