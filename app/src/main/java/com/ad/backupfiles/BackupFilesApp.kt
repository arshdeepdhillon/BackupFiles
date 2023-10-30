package com.ad.backupfiles

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ad.backupfiles.ui.navigation.BackupFilesNavHost

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

@Composable
fun BackupFilesApp(navController: NavHostController = rememberNavController()) {
    BackupFilesNavHost(navController = navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupFilesTopAppBar(
    title: String,
    canNavBack: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavUp: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canNavBack) {
                IconButton(onClick = onNavUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back_button)
                    )
                }
            }
        }
    )
}