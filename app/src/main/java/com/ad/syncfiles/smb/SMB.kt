package com.ad.syncfiles.smb

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare


class SMB {
    companion object {
        private var mClient: SMBClient? = null
        private fun getInstance(): SMBClient {
            if (mClient == null) {
                mClient = SMBClient()
            }
            return mClient!!
        }
    }


    fun listFiles() {

//        try {
        Thread {
            getInstance().connect("192.168.1.95").use { connection ->
                val ac = AuthenticationContext("ad", "adhil8211".toCharArray(), "WORKGROUP")
                val session: Session = connection.authenticate(ac)

                val sharedFiles: DiskShare = session.connectShare("shared-folder") as DiskShare
                for (f: FileIdBothDirectoryInformation in sharedFiles.list("", "*")) {
                    println("File : " + f.fileName)
                }
            }
        }.start()
//        } catch (e: IOException) {
//            System.out.println(e.message)
//        }
    }


    fun funz() {
        getInstance().connect("192.168.1.95").use { connection ->
            val ac = AuthenticationContext("USERNAME", "PASSWORD".toCharArray(), "DOMAIN")
            val session = connection.authenticate(ac)
            val sharedFiles: DiskShare = session.connectShare("shared-folder") as DiskShare

//            sharedFiles.rm()
//            for (f: FileIdBothDirectoryInformation in sharedFiles.list("", "*")) {
//                println("File : " + f.rm("FILE"))
//            }
        }
    }
}
