package com.ad.backupfiles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ad.backupfiles.ui.navigation.BackupFilesNavHost
import com.ad.backupfiles.ui.theme.BackupFilesTheme

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BackupFilesTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { testTagsAsResourceId = true },
                    color = MaterialTheme.colorScheme.background,
                ) {
                    BackupFilesApp()
                }
            }
        }
    }
}

@Composable
fun BackupFilesApp(navController: NavHostController = rememberNavController()) {
    BackupFilesNavHost(navController = navController)
}
