package com.ad.syncfiles

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

object Util {

    /**
     * Displays a toast message with the specified string resource.
     * @param context the Android context in which to display the toast.
     * @param resourceId the resource ID of the string to be displayed as the toast message.
     * @param duration the duration for which the toast should be displayed (default is [Toast.LENGTH_SHORT]).
     */
    fun makeToast(
        context: Context,
        @StringRes resourceId: Int,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        toast(context, context.getText(resourceId).toString(), duration)
    }

    /**
     * Displays a toast message with the specified message and duration.
     */
    private fun toast(context: Context, msg: String, lengthShort: Int) {
        Toast.makeText(context, msg, lengthShort).show()
    }
}