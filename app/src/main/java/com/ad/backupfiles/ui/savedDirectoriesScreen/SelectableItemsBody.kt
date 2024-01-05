package com.ad.backupfiles.ui.savedDirectoriesScreen

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ad.backupfiles.R
import com.ad.backupfiles.data.entity.DirectoryDto
import com.ad.backupfiles.ui.theme.BackupFilesTheme
import com.ad.backupfiles.ui.utils.TestTag.Companion.CHECK_BOX
import com.ad.backupfiles.ui.utils.TestTag.Companion.LAZY_COLUMN_TAG
import com.ad.backupfiles.ui.utils.TestTag.Companion.SYNC_STATUS_TITLE
import java.text.DateFormat
import java.time.Instant
import java.util.Locale

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Displays the given list of data in a single column
 *
 * @param modifier The modifier to apply to the composable.
 * @param savedDirs The list of saved directories to display.
 * @param onItemSelect Invoked when an item is clicked in selection mode.
 * @param resetSelectionState Whether to reset the selection state. Defaults to `false`.
 * @param onSelectionModeChange Invoked when the selection mode changes.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectableItemsBody(
    modifier: Modifier = Modifier,
    savedDirs: List<DirectoryDto>,
    onItemSelect: (Pair<Boolean, DirectoryDto>) -> Unit = {},
    resetSelectionState: Boolean = false,
    onSelectionModeChange: (Boolean) -> Unit = {},
) {
    // rememberSavable to save the state across configuration changes
    var selectedDirIds by rememberSaveable { mutableStateOf(emptySet<Long>()) }
    val isSelectionMode by remember { derivedStateOf { selectedDirIds.isNotEmpty() } }

    DisposableEffect(key1 = isSelectionMode, key2 = resetSelectionState) {
        if (resetSelectionState) {
            selectedDirIds = emptySet()
        } else { // Update the parents
            onSelectionModeChange(isSelectionMode)
        }
        // Clean up resources (if any) when the effect leaves the Composition
        onDispose {
            if (!isSelectionMode && selectedDirIds.isNotEmpty()) {
                selectedDirIds = emptySet()
            }
        }
    }

    LazyVerticalGrid(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.content_layout_pad))
            .testTag(LAZY_COLUMN_TAG),
        columns = GridCells.Fixed(1),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(items = savedDirs) { item ->
            val selected by remember { derivedStateOf { item.dirId in selectedDirIds } }
            ItemDetails(
                modifier = if (isSelectionMode) {
                    Modifier.clickable {
                        if (selected) {
                            selectedDirIds -= item.dirId
                            onItemSelect(Pair(false, item))
                        } else {
                            selectedDirIds += item.dirId
                            onItemSelect(Pair(true, item))
                        }
                    }
                } else {
                    Modifier.combinedClickable(onClick = { }, onLongClick = {
                        selectedDirIds += item.dirId
                        // Selection mode has started, make sure we also add the initial item to our list
                        onItemSelect(Pair(true, item))
                    })
                },
                directoryName = item.dirName,
                lastSynced = item.lastSynced,
                selected = selected,
                isSelectedMode = isSelectionMode,
            )
        }
    }
}

/**
 * Displays details of a given directory
 *
 * @param modifier The modifier for customizing the layout of the composable.
 * @param directoryName The name of the directory.
 * @param lastSynced The timestamp of the last synchronization.
 * @param selected Whether the item is currently selected.
 * @param isSelectedMode Whether the selection mode is active.
 */
@Composable
fun ItemDetails(
    modifier: Modifier = Modifier,
    directoryName: String?,
    lastSynced: Long?,
    selected: Boolean,
    isSelectedMode: Boolean,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = isSelectedMode,
            enter = slideInHorizontally(initialOffsetX = { fullWidth ->
                // Slide in from left
                -fullWidth
            }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { fullWidth ->
                // Slide out to left
                -fullWidth
            }) + shrinkHorizontally() + fadeOut(),
        ) {
            CircleCheckBox(
                modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.m_pad))
                    .testTag(CHECK_BOX),
                selected,
            )
        }
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(size = 6.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(id = R.dimen.s_pad)),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.folder_24),
                        contentDescription = "Content Type",
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.s_pad)),
                    )
                    Column {
                        Text(text = directoryName ?: "")
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                modifier = Modifier.testTag(SYNC_STATUS_TITLE),
                                text = stringResource(R.string.sync_status_title) + (
                                    formatDate(lastSynced)
                                        ?: stringResource(R.string.not_yet_sync_status)
                                    ),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Represents a circle-shaped checkbox.
 *
 * @param modifier The modifier for customizing the layout of the composable.
 * @param selected Whether the checkbox is selected or not.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CircleCheckBox(modifier: Modifier = Modifier, selected: Boolean) {
    AnimatedContent(targetState = selected, label = "Item selection icon", transitionSpec = {
        if (targetState) {
            scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ) with
                scaleOut(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                )
        } else {
            scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ) with
                scaleOut(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                )
        }
    }) { targetInSelectionMode ->
        Box(contentAlignment = Alignment.Center, modifier = modifier.clip(CircleShape)) {
            Icon(
                imageVector = if (targetInSelectionMode) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                contentDescription = stringResource(if (targetInSelectionMode) R.string.icon_checked else R.string.icon_unchecked),
            )
        }
    }
}

fun formatDate(timestampSec: Long?): String? {
    if (timestampSec == null) {
        return null
    }
    val locale = Locale.getDefault()
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale)
        .format(timestampSec * 1000)
}

@Preview(showBackground = true)
@Composable
fun SelectableItemsBodyPreview() {
    val oneDayInSec = 86_400
    SelectableItemsBody(
        savedDirs = (0L..5L).toList().map {
            DirectoryDto(
                dirId = it,
                smbServerId = 0,
                dirPath = "/some/dir/path/$it",
                lastSynced = if ((it % 2).toInt() == 0) null else Instant.now().epochSecond - oneDayInSec * it,
                dirName = "Directory name $it",
            )
        },
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
