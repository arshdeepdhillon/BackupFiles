package com.ad.backupfiles.ui.utils

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.ad.backupfiles.R
import com.ad.backupfiles.data.entity.DirectoryDto
import com.ad.backupfiles.ui.theme.BackupFilesTheme
import java.io.File
import java.text.DateFormat
import java.util.Locale

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Displays the given list of data in a single column
 *
 * @param modifier The modifier to apply to the composable.
 * @param fileList The list of saved folders to display.
 * @param onItemSelect Invoked when an item is clicked in selection mode.
 * @param onSelectionModeChange Invoked when the selection mode changes.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemListBody(
    modifier: Modifier = Modifier,
    fileList: List<DirectoryDto>,
    onItemSelect: (Pair<Boolean, DirectoryDto>) -> Unit = {},
    resetSelectionState: Boolean = false,
    onSelectionModeChange: (Boolean) -> Unit = {},
) {
    // rememberSavable to save the state across configuration changes
    var selectedUris by rememberSaveable { mutableStateOf(emptySet<Uri>()) }
    val isSelectionMode by remember { derivedStateOf { selectedUris.isNotEmpty() } }

    DisposableEffect(key1 = isSelectionMode, key2 = resetSelectionState) {
        if (resetSelectionState) {
            selectedUris = emptySet()
        } else {// Update the parents
            onSelectionModeChange(isSelectionMode)
        }
        // Clean up resources (if any) when the effect leaves the Composition
        onDispose {
            if (!isSelectionMode && selectedUris.isNotEmpty()) {
                selectedUris = emptySet()
            }
        }
    }

    // Handle back press when in selection mode
    BackHandler(enabled = isSelectionMode) {
        // Clear selected items when back button is pressed
        selectedUris = emptySet()
    }

    LazyVerticalGrid(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.content_layout_pad)),
        columns = GridCells.Fixed(1),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items = fileList) { item ->
            val selected by remember { derivedStateOf { item.dirUri in selectedUris } }
            ItemDetails(
                modifier = if (isSelectionMode) {
                    Modifier.clickable {
                        if (selected) {
                            selectedUris -= item.dirUri
                            onItemSelect(Pair(false, item))
                        } else {
                            selectedUris += item.dirUri
                            onItemSelect(Pair(true, item))
                        }
                    }
                } else {
                    Modifier.combinedClickable(onClick = { }, onLongClick = {
                        selectedUris += item.dirUri
                        // Selection mode has started, make sure we also add the initial item to our list
                        onItemSelect(Pair(true, item))
                    })
                },
                itemName = item.dirName,
                modifiedTime = item.lastModified,
                numOfFiles = item.itemCount,
                isDir = item.isDirectory,
                selected = selected,
                isSelectedMode = isSelectionMode
            )

        }
    }
}


@Composable
fun ItemDetails(
    modifier: Modifier = Modifier,
    itemName: String,
    modifiedTime: Long,
    isDir: Boolean,
    selected: Boolean,
    isSelectedMode: Boolean,
    numOfFiles: Int?,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AnimatedVisibility(
            visible = isSelectedMode,
            enter = slideInHorizontally(initialOffsetX = { fullWidth ->
                //Slide in from left
                -fullWidth
            }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { fullWidth ->
                //Slide out to left
                -fullWidth
            }) + shrinkHorizontally() + fadeOut(),
        ) {
            CircleCheckBox(modifier.padding(horizontal = dimensionResource(id = R.dimen.m_pad)), selected)
        }
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(size = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(id = R.dimen.s_pad)),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = if (isDir) painterResource(id = R.drawable.folder_24) else painterResource(
                            id = R.drawable.text_snippet_24
                        ),
                        contentDescription = "Content Type",
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.s_pad))
                    )
                    Column {
                        Text(text = itemName)
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CircleCheckBox(modifier: Modifier = Modifier, selected: Boolean) {
    AnimatedContent(targetState = selected, label = "Item selection icon", transitionSpec = {
        if (targetState) {
            scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) with
                    scaleOut(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium))
        } else {
            scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)) with
                    scaleOut(animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium))
        }
    }) { targetInSelectionMode ->
        Box(contentAlignment = Alignment.Center, modifier = modifier.clip(CircleShape)) {
            Icon(
                imageVector = if (targetInSelectionMode) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                contentDescription = stringResource(R.string.icon_check)
            )
        }

    }
}


fun formatDate(timestampMillis: Long): String {
    val locale = Locale.getDefault()
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale)
        .format(timestampMillis)
}


@Preview(showBackground = true)
@Composable
fun ItemListBodyPreview() {
    ItemListBody(
        fileList = (0..10).toList().map {
            DocumentFile.fromFile(File("temp${it}")).let { tempDir ->
                DirectoryDto(
                    dirId = it,
                    dirUri = tempDir.uri,
                    isDirectory = tempDir.isDirectory,
                    itemCount = tempDir.listFiles().size,
                    dirName = if (tempDir.name == null) "unknowfoldername" else tempDir.name!!,
                    lastModified = tempDir.lastModified()
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CircleCheckBoxPreview() {
    BackupFilesTheme {
        Column {
            CircleCheckBox(selected = false)
            CircleCheckBox(selected = true)
        }
    }
}
