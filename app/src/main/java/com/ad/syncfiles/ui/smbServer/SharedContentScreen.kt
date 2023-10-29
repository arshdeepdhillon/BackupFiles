package com.ad.syncfiles.ui.smbServer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.ad.syncfiles.Util
import com.ad.syncfiles.data.entity.SmbServerInfo
import com.ad.syncfiles.ui.AppViewModelProvider
import com.ad.syncfiles.ui.navigation.NavigationDestination
import com.ad.syncfiles.ui.theme.SyncFilesTheme
import com.ad.syncfiles.ui.utils.DeleteAlert
import com.ad.syncfiles.ui.utils.ItemListBody
import kotlinx.coroutines.launch

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */


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


const val TAG: String = "SharedContentScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SharedContentScreen(
    handleNavUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SharedContentScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var inSelectionMode by rememberSaveable { mutableStateOf(false) }
    var isDeleteDialogActive by rememberSaveable { mutableStateOf(false) }
    val dirPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { dirUri: Uri? ->
            coroutineScope.launch {
                if (dirUri == null) {
                    Util.makeToast(context, R.string.null_uri)
                } else {
                    // Persist the permission of this Uri inorder to access it after a app/phone restart
                    context.contentResolver.takePersistableUriPermission(
                        dirUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    viewModel.saveDirectory(dirUri)
                }
            }
        }

    Scaffold(
        topBar = {
            AnimatedContent(targetState = inSelectionMode, transitionSpec = {
                if (targetState) {
                    fadeIn() with fadeOut()
                } else {
                    fadeIn() with fadeOut()
                }.using(
                    // Disable clipping since the faded slide-in/out should be displayed out of bounds.
                    SizeTransform(clip = false)
                )
            }, label = "additional settings") { targetInSelectionMode ->
                if (targetInSelectionMode) {
                    AdditionalSettings {
                        isDeleteDialogActive = true
                    }
                } else {
                    SyncFilesTopAppBar(
                        title = stringResource(SharedContentScreenDestination.titleRes),
                        canNavBack = true,
                        onNavUp = handleNavUp
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !inSelectionMode,
                enter = slideInHorizontally(initialOffsetX = { fullWidth ->
                    // Slide from right to left
                    fullWidth
                }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { fullWidth ->
                    // Slide from left to right
                    fullWidth
                }) + shrinkHorizontally() + fadeOut(),
            ) {
                FloatingActionButton(
                    onClick = {
                        dirPickerLauncher.launch(null)
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.m_pad))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.add_connection)
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        ItemListBody(
            modifier = Modifier.padding(innerPadding),
            fileList = getDocument(context, uiState.content),
            onItemClick = { item: Pair<DocumentFile, Boolean> ->
                Log.d(TAG, "Item clicked!: ${item.first}")
                viewModel.onItemClick(item)
            },
            onSelectionModeChange = {
                inSelectionMode = it
            }
        )
        if (isDeleteDialogActive) {
            DeleteAlert(
                handleAccept = {
                    isDeleteDialogActive = false
                    viewModel.deleteSelected()
                },
                handleCancel = { isDeleteDialogActive = false },
                modifier = Modifier.padding(dimensionResource(id = R.dimen.m_pad))
            )
        }
    }
}


/**
 * Converts list of [Uri] into list of [DocumentFile]
 * TODO: is this needed?
 */
fun getDocument(context: Context, uriPaths: List<String>): List<DocumentFile> {
    return uriPaths
        .mapNotNull { uriPath ->
            DocumentFile.fromTreeUri(context, Uri.parse(uriPath))
        }.toList()
        .ifEmpty { emptyList() }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdditionalSettings(onDelete: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { },
        actions = {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete selected items"
                )
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
private fun SharedContentScreenPreview() {
    SyncFilesTheme {
        SharedContentScreen(handleNavUp = {})
    }
}

