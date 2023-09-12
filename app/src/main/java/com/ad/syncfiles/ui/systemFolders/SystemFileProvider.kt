package com.ad.syncfiles.ui.systemFolders

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun SystemFileProvider(modifier: Modifier = Modifier) {
    var selectedDirUri by remember { mutableStateOf<Uri?>(null) }
    val dirPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        selectedDirUri = uri
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Selected Directory: ${selectedDirUri?.path ?: "None"}",
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                dirPickerLauncher.launch(null)
            }
        ) {
            Text(text = "Select Directory")
        }
    }
}

//@Composable
//fun SystemFileProvider(modifier: Modifier = Modifier) {
//    var selectedDirUri by remember { mutableStateOf<Uri?>(null) }
//    val dirPickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.OpenDocumentTree()
//    ) { uri ->
//        selectedDirUri = uri
//    }
//
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = "Selected Directory: ${selectedDirUri?.path ?: "None"}",
//            style = MaterialTheme.typography.labelSmall
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(
//            onClick = {
//                dirPickerLauncher.launch(null)
//            }
//        ) {
//            Text(text = "Select Directory")
//        }
//    }
//}


//class MyPreviewProvider : PreviewParameterProvider<MyAppPreviewParameters> {
//    override val values: Sequence<MyAppPreviewParameters>
//        get() = sequenceOf(MyAppPreviewParameters())
//}

//data class MyAppPreviewParameters(
//    val selectedDirectoryUri: Uri? = null,
//)

//@Preview(showBackground = true)
//@Composable
//fun MyAppWithPreview() {
//    val selectedDirectoryUri = remember { Uri.parse("/storage/emulated/0/Download") } // Provide a sample Uri for preview
//    MyAppPreviewParameters(selectedDirectoryUri)
//}

//@Preview(showBackground = true)
//@Composable
//fun MyAppPreview(@PreviewParameter(MyPreviewProvider::class) params: MyAppPreviewParameters) {
//    SystemFileProvider()
//}