package com.ad.syncfiles.ui.smbServer

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
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ad.syncfiles.R
import com.ad.syncfiles.SyncFilesTopAppBar
import com.ad.syncfiles.Util
import com.ad.syncfiles.data.entity.DirectoryDto
import com.ad.syncfiles.data.entity.SmbServerInfo
import com.ad.syncfiles.ui.AppViewModelProvider
import com.ad.syncfiles.ui.navigation.NavigationDestination
import com.ad.syncfiles.ui.theme.SyncFilesTheme
import com.ad.syncfiles.ui.utils.GeneralAlert
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
    var isSyncDialogActive by rememberSaveable { mutableStateOf(false) }
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
                        isSyncDialogActive = true
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
            fileList = uiState.content,
            onItemSelect = { item: Pair<Boolean, DirectoryDto> ->
                Log.d(TAG, "Item clicked!: ${item.first}")
                viewModel.handleSelected(item)
            }
        ) {
            inSelectionMode = it
        }
        if (isSyncDialogActive) {
            GeneralAlert(
                handleAccept = {
                    isSyncDialogActive = false
                    viewModel.syncSelectedFolder()
                    //TODO clear the inSelectionMode state so the selected items are cleared
                },
                titleId = R.string.confirm_title_alert,
                bodyId = R.string.sync_body_alert,
                handleCancel = { isSyncDialogActive = false },
                modifier = Modifier.padding(dimensionResource(id = R.dimen.m_pad))
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdditionalSettings(onSyncClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { },
        actions = {
            IconButton(onClick = onSyncClick) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Save newly added content in selected folder on SMB server"
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

