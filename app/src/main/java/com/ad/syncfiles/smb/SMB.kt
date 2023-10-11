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
import com.hierynomus.smbj.common.SMBRuntimeException
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.smbj.share.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Deque


class SMB : SMBApi {
    private val TAG = SMB::class.java.simpleName

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
     * Get a DiskShare.
     *
     * @return A DiskShare instance.
     * @throws IOException If the connection could not be established.
     */
    @Throws(IOException::class)
    private fun getDiskShare(): DiskShare {
        val ac = AuthenticationContext("ad", "adhil8211".toCharArray(), "WORKGROUP")
        return getInstance().connect("192.168.1.95").authenticate(ac).connectShare("shared-folder") as DiskShare
    }


    /**
     * Copies the files and its subdirectories from the specified [uri] and saves it (TODO) to the provided SMB server.
     *
     * @param context The Android context used for accessing resources and file operations.
     * @param uri The [Uri] representing the folder to be saved.
     */
    override suspend fun saveFolder(context: Context, uri: Uri) {
        val dirName = FileUtils.getDirName(context, uri) ?: return
        val dirContentList: Deque<DocumentFile> = FileUtils.getFilesInDir(context, uri)

        if (dirContentList.isEmpty()) {
            return
        }
        getDiskShare().use { diskShare ->
            Log.d(TAG, "Folder exists on SMB server?: " + diskShare.folderExists(dirName))
            //Create the directory
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
                saveSingleFile(context, docOnDevice, path, diskShare)
            }
        }

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
    private fun saveSingleFile(context: Context, docOnDevice: DocumentFile, path: String, diskShare: DiskShare) {
        context.contentResolver.openInputStream(docOnDevice.uri).use { inStream ->
            if (inStream != null) {
                // Create file on SMB server
                val fileOnSmbServer = diskShare.openFile(
                    path,
                    setOf<AccessMask>(AccessMask.FILE_WRITE_DATA),
                    setOf<FileAttributes>(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                    setOf<SMB2ShareAccess>(SMB2ShareAccess.FILE_SHARE_WRITE),
                    SMB2CreateDisposition.FILE_OVERWRITE_IF, //TODO change it to SMB2CreateDisposition.FILE_CREATE
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
            }
        }
    }
}
