package com.ad.syncfiles.data.entity

import android.net.Uri
import java.util.Date

data class MediaStoreContent(
    val id: Long,
    val displayName: String,
    val dateAdded: Date,
    val contentUri: Uri,
)
