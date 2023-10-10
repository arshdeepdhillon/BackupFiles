package com.ad.syncfiles.smb

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.Directory
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Base64
import java.util.Deque


class SMB : SMBApi {
    private val TAG = Companion::class.java.simpleName

    companion object {
        @Volatile
        private var mClient: SMBClient? = null
        private fun getInstance(): SMBClient {
            if (mClient == null) {
                synchronized(this) {
                    mClient = SMBClient()
                }
            }
            return mClient!!
        }
    }

    private fun getDiskShare(): DiskShare {
        val ac = AuthenticationContext("ad", "adhil8211".toCharArray(), "WORKGROUP")
        return getInstance().connect("192.168.1.95").authenticate(ac).connectShare("shared-folder") as DiskShare


//        return withContext(Dispatchers.IO) {
//            getInstance().connect("192.168.1.95").use { connection ->
//                val ac = AuthenticationContext("ad", "adhil8211".toCharArray(), "WORKGROUP")
//                val session: Session = connection.authenticate(ac)
//                session.connectShare("shared-folder") as DiskShare
//            }
//        }
    }

    private fun save(
        stringData: String, path: String, fileShare: DiskShare,
    ): String? {
        var pathResponse: String? = null
        val encodedString: String = Base64.getEncoder().encodeToString(stringData.toByteArray())
        val data: ByteArray = Base64.getDecoder().decode(encodedString)
        try {
            fileShare.openFile(
                path,
                setOf<AccessMask>(AccessMask.FILE_WRITE_DATA),
                setOf<FileAttributes>(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                setOf<SMB2ShareAccess>(SMB2ShareAccess.FILE_SHARE_WRITE),
                SMB2CreateDisposition.FILE_OVERWRITE_IF,
                setOf<SMB2CreateOptions>(SMB2CreateOptions.FILE_SEQUENTIAL_ONLY)
            ).use { file ->
                file?.outputStream?.use { outputStream ->
                    outputStream.write(data)
                    outputStream.flush()
                    outputStream.close()
                    pathResponse = path
                }
            }
        } catch (e: Exception) {
            throw e
        }
        return pathResponse
    }

    override suspend fun saveFolder(context: Context, uri: Uri) {
        withContext(Dispatchers.IO) {
            val dirName = FileUtils.getDirName(context, uri)
            if (dirName != null) {
                val dirContentList: Deque<DocumentFile> = FileUtils.getFilesInDir(context, uri)
                getDiskShare().use { diskShare ->
                    Log.d(TAG, "Folder exist: " + diskShare.folderExists(dirName))
                    try {
                        diskShare.openDirectory(
                            dirName,
                            setOf<AccessMask>(AccessMask.FILE_WRITE_DATA),
                            setOf<FileAttributes>(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                            setOf<SMB2ShareAccess>(SMB2ShareAccess.FILE_SHARE_WRITE),
                            SMB2CreateDisposition.FILE_OPEN_IF,
                            setOf<SMB2CreateOptions>(SMB2CreateOptions.FILE_SEQUENTIAL_ONLY)
                        ).use { file: Directory ->
                            val dirInfo = file
                            Log.d(TAG, "Folder fileInformation: ${dirInfo.fileInformation}")
                            Log.d(TAG, "Folder            path: ${dirInfo.path}")
                            Log.d(TAG, "Folder         dirInfo: $dirInfo")
                            var path = ""
                            while (dirContentList.isNotEmpty()) {
                                val fileOnDisk = dirContentList.poll()
                                path = "$dirName\\${fileOnDisk.name}"
                                context.contentResolver.openInputStream(fileOnDisk.uri).use { fOnDiskStream ->
                                    if (fOnDiskStream != null) {
                                        val data: ByteArray = fOnDiskStream.readBytes()
                                        getDiskShare().openFile(
                                            path,
                                            setOf<AccessMask>(AccessMask.FILE_WRITE_DATA),
                                            setOf<FileAttributes>(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                                            setOf<SMB2ShareAccess>(SMB2ShareAccess.FILE_SHARE_WRITE),
                                            SMB2CreateDisposition.FILE_OVERWRITE_IF,
                                            setOf<SMB2CreateOptions>(SMB2CreateOptions.FILE_SEQUENTIAL_ONLY)
                                        ).use { file ->
                                            file?.outputStream?.use { outputStream ->
                                                outputStream.write(data)
                                                outputStream.flush()
                                                outputStream.close()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to save $dirName", e)
                    }
                }
            }
        }
    }
}
