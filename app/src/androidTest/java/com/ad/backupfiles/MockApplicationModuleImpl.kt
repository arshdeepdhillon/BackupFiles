package com.ad.backupfiles

import android.content.Context
import com.ad.backupfiles.data.dao.DirectoryDao
import com.ad.backupfiles.data.dao.SmbServerDao
import com.ad.backupfiles.data.repository.OfflineDirectoryRepoImpl
import com.ad.backupfiles.data.repository.OfflineSmbServerRepoImpl
import com.ad.backupfiles.data.repository.api.DirectoryInfoApi
import com.ad.backupfiles.data.repository.api.SmbServerInfoApi
import com.ad.backupfiles.di.api.ApplicationModuleApi
import com.ad.backupfiles.smb.api.SMBClientApi
import kotlinx.coroutines.CoroutineScope

/*
 * @author : Arshdeep Dhillon
 * @created : 29-Dec-23
*/
class MockApplicationModuleImpl(
    private val context: Context,
    private val testScope: CoroutineScope,
    private val dirDao: DirectoryDao,
    private val smbDao: SmbServerDao,
    private val smbClient: SMBClientApi,
) : ApplicationModuleApi {

    override val smbServerApi: SmbServerInfoApi by lazy {
        OfflineSmbServerRepoImpl(smbDao)
    }
    override val directoryInfoApi: DirectoryInfoApi by lazy {
        OfflineDirectoryRepoImpl(dirDao, testScope)
    }

    override val appContext: Context by lazy {
        context
    }

    override val smbClientApi: SMBClientApi by lazy {
        smbClient
    }
}
