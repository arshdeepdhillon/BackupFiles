package com.ad.backupfiles

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

object UiUtil {

    private const val MAX_ARG_LEN = 20
    private const val MAX_TRUNK_LEN = 15

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
            context, context.getString(resourceId, *padArgs(args)), duration
        )
    }

    /**
     * Truncate and ellipsis long strings if they exceed [MAX_ARG_LEN].
     *
     * @param args The list of nullable strings to process.
     * @return An array of processed strings.
     */
    private fun padArgs(args: List<String?>): Array<String> {
        return args.filterNotNull().map { str ->
            if (str.length > MAX_ARG_LEN) str.take(MAX_TRUNK_LEN).trim() + ".." else str
        }.toTypedArray()
    }

    /**
     * Displays a toast message with the specified message and duration.
     */
    private fun toast(context: Context, msg: String, lengthShort: Int) {
        Toast.makeText(context, msg, lengthShort).show()
    }
}