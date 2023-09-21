package com.ad.syncfiles.ui.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.ad.syncfiles.R
import java.text.DateFormat
import java.util.Locale

/**
 * Displays the given list of data in a single column
 */
@Composable
fun ItemListBody(modifier: Modifier = Modifier, fileList: List<DocumentFile>, onItemClick: (DocumentFile) -> Unit = {}) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(1), modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.small_padding)),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items = fileList) { item ->
            ItemDetails(
                modifier = Modifier.clickable { onItemClick(item) },
                itemName = item.name.toString(),
                modifiedTime = item.lastModified(),
                numOfFiles = item.listFiles().size,
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
fun ItemListBodyPreview() {
//    ItemListBody(
//        fileList = (0..10).toList().map { File("temp${it}") }
//    )
}