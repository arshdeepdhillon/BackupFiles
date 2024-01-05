package com.ad.backupfiles

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * Utility object for displaying Toast messages in the application.
 */
object Toast {

    /** String is truncated if it exceeds this limit. */
    private const val MAX_STR_LENGTH = 20

    /** Maximum length of string to keep. */
    private const val MAX_TRUNCATE_LEN = 15

    /**
     * Displays a toast message with the specified string resource.
     * @param context the Android context in which to display the toast.
     * @param resourceId the resource ID of the string to be displayed as the toast message.
     * @param duration the duration for which the toast should be displayed (default is [Toast.LENGTH_SHORT]).
     */
    fun makeToast(
        context: Context,
        @StringRes resourceId: Int,
        args: List<String?> = emptyList(),
        duration: Int = Toast.LENGTH_SHORT,
    ) {
        toast(
            context,
            context.getString(resourceId, *padArgs(args)),
            duration,
        )
    }

    /**
     * Truncate and ellipsis long strings if they exceed [MAX_STR_LENGTH].
     *
     * @param args The list of nullable strings to process.
     * @return An array of processed strings.
     */
    private fun padArgs(args: List<String?>): Array<String> {
        return args.filterNotNull().map { str ->
            if (str.length > MAX_STR_LENGTH) str.take(MAX_TRUNCATE_LEN).trim() + ".." else str
        }.toTypedArray()
    }

    /**
     * Displays a toast message with the specified message and duration.
     */
    private fun toast(context: Context, msg: String, lengthShort: Int) {
        Toast.makeText(context, msg, lengthShort).show()
    }
}
