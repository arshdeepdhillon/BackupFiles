package com.ad.syncfiles.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.ad.syncfiles.R

@Composable
fun SharedDirScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(dimensionResource(R.dimen.md_pad))) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
        ) {
            Text(modifier = Modifier.fillMaxWidth(), text = "testing1")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SharedDirScreenPreview() {
    SharedDirScreen(modifier = Modifier.fillMaxSize())
}
