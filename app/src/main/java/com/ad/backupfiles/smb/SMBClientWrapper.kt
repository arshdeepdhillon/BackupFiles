package com.ad.backupfiles.smb

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.ad.backupfiles.data.entity.SmbServerDto
import com.ad.backupfiles.smb.api.SMBClientApi
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mserref.NtStatus
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.common.SMBRuntimeException
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.smbj.share.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Deque

/*
 * @author : Arshdeep Dhillon
 * @created : 23-Oct-23
 */

/**
 * This wrapper class serves as an interface to interact with SMB server and provides
 * methods for various SMB-related operations.
 *
 * @see SMBClientApi
 */
class SMBClientWrapper : SMBClientApi {
    private val TAG = SMBClientWrapper::class.java.simpleName

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

    /**
     * Connects to a network share on a remote server using the provided credentials.
     * @param username The username for authentication.
     * @param password The password for authentication.
     * @param serverAddress The address of the remote server.
     * @param sharedFolder The name of the shared folder to connect to.
     * @return A [DiskShare] representing the connected share.
     * @throws IOException if an I/O error occurs during the connection.
     */
    @Throws(IOException::class)
    private fun getDiskShare(
        username: String,
        password: String,
        serverAddress: String,
        sharedFolder: String,
    ): DiskShare {
        val ac = AuthenticationContext(username, password.toCharArray(), "WORKGROUP")
        return getInstance().connect(serverAddress).authenticate(ac)
            .connectShare(sharedFolder) as DiskShare
    }


    /**
     * Saves a single file from a DocumentFile to a specified path on SMB server using a provided DiskShare.
     *
     * @param context The context.
     * @param docOnDevice The DocumentFile representing the file to be saved.
     * @param path The destination path on disk where the file will be saved.
     * @param diskShare The DiskShare used for performing the save operation.
     *
     * @throws SMBRuntimeException if there is an SMB-specific runtime exception.
     * @throws IOException if we are unable to read from a file and write to a file.
     * @throws FileNotFoundException if there is no data associated with the [Uri] of [docOnDevice].
     */
    @Throws(SMBRuntimeException::class, IOException::class, FileNotFoundException::class)
    private fun saveSingleFile(
        context: Context,
        docOnDevice: DocumentFile,
        path: String,
        diskShare: DiskShare,
        isSync: Boolean,
    ) {
        context.contentResolver.openInputStream(docOnDevice.uri).use { inStream ->
            if (inStream != null) {
                val fileOnSmbServer: File

                try {
                    // Create file on SMB server
                    fileOnSmbServer = diskShare.openFile(
                        path,
                        setOf<AccessMask>(AccessMask.FILE_WRITE_DATA),
                        setOf<FileAttributes>(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                        setOf<SMB2ShareAccess>(SMB2ShareAccess.FILE_SHARE_WRITE),
                        if (isSync) SMB2CreateDisposition.FILE_CREATE else SMB2CreateDisposition.FILE_OVERWRITE_IF,
                        setOf<SMB2CreateOptions>(SMB2CreateOptions.FILE_SEQUENTIAL_ONLY)
                    )
                    var prog: Int
                    var totalBytesRead = 0
                    val size: Long = docOnDevice.length()
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesRead = inStream.read(buffer)

                    fileOnSmbServer.use { file: File ->
                        file.outputStream.use { outputStream ->
                            Log.d(TAG, "Saving file...")
                            while (bytesRead >= 0) {
                                outputStream.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                                prog = ((totalBytesRead.toFloat() / size) * 100).toInt()
                                if (prog % 10 == 0) {
                                    Log.d(TAG, "Progress $prog")
                                }
                                bytesRead = inStream.read(buffer)
                            }
                            outputStream.flush()
                            Log.d(TAG, "Saving file...Done")
                        }
                    }

                } catch (e: SMBApiException) {
                    // If content already exists and we're in sync mode, then continue otherwise raise it
                    if (!(isSync && e.statusCode == NtStatus.STATUS_OBJECT_NAME_COLLISION.value)) {
                        throw (e)
                    }
                }
            }
        }
    }

    override suspend fun saveFolder(
        context: Context,
        smbServerDto: SmbServerDto,
        folderToSave: String,
        isSync: Boolean,
    ) {
        val (username, password, serverAddress, sharedFolder) = smbServerDto
        val dirName = FileUtils.getDirName(context, folderToSave) ?: return
        val dirContentList: Deque<DocumentFile> = FileUtils.getFilesInDir(context, folderToSave)

        getDiskShare(username, password, serverAddress, sharedFolder).use { diskShare ->
            Log.d(TAG, "Folder exists on SMB server?: " + diskShare.folderExists(dirName))
            // Create the directory even if its empty
            diskShare.openDirectory(
                dirName,
                setOf<AccessMask>(AccessMask.FILE_WRITE_DATA),
                setOf<FileAttributes>(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                setOf<SMB2ShareAccess>(SMB2ShareAccess.FILE_SHARE_WRITE),
                SMB2CreateDisposition.FILE_OPEN_IF,
                setOf<SMB2CreateOptions>(SMB2CreateOptions.FILE_SEQUENTIAL_ONLY)
            )
            var path: String
            while (dirContentList.isNotEmpty()) {
                Log.d(TAG, "Files remaining ${dirContentList.size}")
                val docOnDevice = dirContentList.poll()
                // We should have read permission (on Uri) at this point, if not (then 'name' will be null) then complain loudly
                path = "$dirName\\${docOnDevice!!.name}"
                saveSingleFile(context, docOnDevice, path, diskShare, isSync)
            }
        }

    }

    override suspend fun canConnect(smbServerDto: SmbServerDto): Boolean {
        val (username, password, serverAddress, sharedFolder) = smbServerDto
        try {
            getDiskShare(username, password, serverAddress, sharedFolder).use {
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect with SMB server", e)
        }
        return false
    }

}
