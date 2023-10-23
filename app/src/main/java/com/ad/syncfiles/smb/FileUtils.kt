package com.ad.syncfiles.smb

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import java.util.Deque
import java.util.LinkedList

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

object FileUtils {

    /**
     * Retrieve the display name of the file from given [Uri].
     * @param context the context
     * @param uri     used to query file name of
     */
    fun getDirName(context: Context, uri: Uri): String? {
        val rootDocument = DocumentFile.fromTreeUri(context, uri)
        return rootDocument?.name
    }

    /**
     * Retrieve the display name of the file from given [Uri].
     * @param context the context
     * @param uri     used to query file name of
     */
    fun getDirName(context: Context, contentPath: String): String? {
        try {
            val uri = Uri.parse(contentPath)
            return getDirName(context, uri)
        } catch (e: NullPointerException) {
            Log.e("GetDirName", "Unable to parse $contentPath", e)
        }
        return null
    }

    fun getFilesInDir(context: Context, uri: Uri): Deque<DocumentFile> {
        val rootDocument = DocumentFile.fromTreeUri(context, uri)
        val dequeList: Deque<DocumentFile> = LinkedList()
        if (rootDocument != null) {
            dequeList.addAll(rootDocument.listFiles())
        }
        return dequeList
    }
}
