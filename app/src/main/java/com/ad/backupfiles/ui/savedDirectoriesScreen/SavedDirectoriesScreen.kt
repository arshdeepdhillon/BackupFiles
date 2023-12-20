package com.ad.backupfiles.ui.savedDirectoriesScreen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ad.backupfiles.R
import com.ad.backupfiles.Toast
import com.ad.backupfiles.data.entity.DirectoryDto
import com.ad.backupfiles.data.entity.SmbServerInfo
import com.ad.backupfiles.di.ApplicationViewModelFactory
import com.ad.backupfiles.ui.navigation.NavigationDestination
import com.ad.backupfiles.ui.savedDirectoriesScreen.SavedDirectoriesViewModel.ErrorUiState
import com.ad.backupfiles.ui.shared.GeneralAlert
import com.ad.backupfiles.ui.shared.SelectableItemsBody
import com.ad.backupfiles.ui.shared.TopAppBar
import com.ad.backupfiles.ui.theme.BackupFilesTheme

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * A stateless singleton representing navigation details
 */
object SavedDirectoriesScreenDestination : NavigationDestination {
    override val route = "saved_directories"
    override val titleRes = R.string.smb_server_saved_directories_title

    /**
     * Used for retrieving a specific [SmbServerInfo] to display
     */
    const val argKey = "smbServerArg"
    val routeArgs = "$route/{$argKey}"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SavedDirectoriesScreen(
    handleNavUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavedDirectoriesViewModel = viewModel(factory = ApplicationViewModelFactory.Factory),
    syncTrackerViewModel: SyncTrackerViewModel = viewModel(factory = ApplicationViewModelFactory.Factory),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var inSelectionMode by rememberSaveable { mutableStateOf(false) }
    var isSyncDialogActive by rememberSaveable { mutableStateOf(false) }
    var clearSelectionState by rememberSaveable { mutableStateOf(false) }
    val dirPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { dirUri: Uri? ->
            // dirUri can be null when no folder is selected
            if (dirUri != null) {
                // Persist the permission of this Uri inorder to access it after a app/phone restart
                context.contentResolver.takePersistableUriPermission(
                    dirUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
                viewModel.saveDirectory(dirUri)
            }
        }

    // Reset the state back to false after it has changed from false (default) -> true
    DisposableEffect(key1 = clearSelectionState) {
        onDispose {
            clearSelectionState = false
        }
    }

    // When in selection mode and back button is pressed, clear selected items
    BackHandler(enabled = inSelectionMode) {
        syncTrackerViewModel.onClearSelectionState()

        // The parent compo ItemListBody know
        clearSelectionState = true
    }

    /* By passing a Unit, it allows us to recompose once when composition starts and doesn't recompose on each collect */
    LaunchedEffect(Unit) {
        viewModel.errorState
            .collect { message ->
                when (message) {
                    is ErrorUiState.Error -> {
                        Toast.makeToast(context, message.resId, message.args)
                    }

                    is ErrorUiState.Empty -> {}
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
                    SizeTransform(clip = false),
                )
            }, label = "additional settings") { targetInSelectionMode ->
                if (targetInSelectionMode) {
                    AdditionalSettings {
                        isSyncDialogActive = true
                    }
                } else {
                    TopAppBar(
                        title = stringResource(SavedDirectoriesScreenDestination.titleRes),
                        canNavBack = true,
                        onNavUp = handleNavUp,
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
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.m_pad)),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.add_connection),
                    )
                }
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        SelectableItemsBody(
            modifier = Modifier.padding(innerPadding),
            savedDirs = uiState.savedDirectories,
            onItemSelect = { item: Pair<Boolean, DirectoryDto> -> syncTrackerViewModel.onDirectorySelected(item) },
            resetSelectionState = clearSelectionState,
        ) {
            inSelectionMode = it
        }
        if (isSyncDialogActive) {
            GeneralAlert(
                handleAccept = {
                    isSyncDialogActive = false // Hide the dialog
                    clearSelectionState = true // Clear the selected items
                    viewModel.syncSelectedFolder(syncTrackerViewModel.directoriesToSync())
                },
                titleId = R.string.confirm_title_alert,
                bodyId = R.string.sync_body_alert,
                handleCancel = { isSyncDialogActive = false },
                modifier = Modifier.padding(dimensionResource(id = R.dimen.m_pad)),
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
                    contentDescription = "Save newly added content in selected folder on SMB server",
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun SavedDirectoriesScreenPreview() {
    BackupFilesTheme {
        SavedDirectoriesScreen(handleNavUp = {})
    }
}
