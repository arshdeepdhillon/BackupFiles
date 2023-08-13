package com.ad.syncfiles

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ad.syncfiles.ui.EmptyMainScreen


@Composable
fun SMBScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        EmptyMainScreen()
    }
}

@Preview
@Composable
fun SMBScreenPreview() {
    SMBScreen()
}