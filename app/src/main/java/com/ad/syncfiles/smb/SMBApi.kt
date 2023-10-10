package com.ad.syncfiles.smb

import android.content.Context
import android.net.Uri

interface SMBApi {
    suspend fun saveFolder(context: Context, uri: Uri)
}