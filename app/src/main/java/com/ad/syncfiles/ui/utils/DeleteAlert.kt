package com.ad.syncfiles.ui.utils

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ad.syncfiles.R
import com.ad.syncfiles.ui.theme.SyncFilesTheme

/*
 * @author : Arshdeep Dhillon
 * @created : 28-Oct-23
*/
/**
 * Composable function to display a delete confirmation alert dialog.
 *
 * @param handleAccept Callback function to handle the user's acceptance of the delete action.
 * @param handleCancel Callback function to handle the user's cancellation of the delete action.
 * @param modifier Modifier for customizing the layout.
 */
@Composable
fun DeleteAlert(handleAccept: () -> Unit, handleCancel: () -> Unit, modifier: Modifier) {
    AlertDialog(onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(R.string.attention)) },
        text = { Text(stringResource(R.string.delete_question)) },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = handleCancel) {
                Text(text = stringResource(R.string.no))
            }
        },
        confirmButton = {
            TextButton(onClick = handleAccept) {
                Text(text = stringResource(R.string.yes))
            }
        }
    )
}

@Preview
@Composable
fun DeleteConfirmationDialogPreview() {
    SyncFilesTheme {
        DeleteAlert(handleAccept = { }, handleCancel = { }, modifier = Modifier)
    }
}