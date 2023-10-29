package com.ad.syncfiles.ui.utils

import androidx.annotation.StringRes
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
 * Composable function to display an alert dialog.
 *
 * @param handleAccept Callback function to handle the user's acceptance of the delete action.
 * @param handleCancel Callback function to handle the user's cancellation of the delete action.
 * @param modifier Modifier for customizing the layout.
 */
@Composable
fun GeneralAlert(handleAccept: () -> Unit, handleCancel: () -> Unit, @StringRes titleId: Int, @StringRes bodyId: Int, modifier: Modifier) {
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text(stringResource(titleId)) },
        text = { Text(stringResource(bodyId)) },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = handleCancel) {
                Text(text = stringResource(R.string.no_alert))
            }
        },
        confirmButton = {
            TextButton(onClick = handleAccept) {
                Text(text = stringResource(R.string.yes_alert))
            }
        }
    )
}

@Preview
@Composable
fun DeleteConfirmationDialogPreview() {
    SyncFilesTheme {
        GeneralAlert(
            handleAccept = { },
            handleCancel = { },
            titleId = R.string.confirm_title_alert,
            bodyId = R.string.delete_body_alert,
            modifier = Modifier
        )
    }
}