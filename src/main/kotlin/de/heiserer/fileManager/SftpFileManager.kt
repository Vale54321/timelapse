package de.heiserer.fileManager

import de.heiserer.SftpClient
import java.io.File

class SftpFileManager(
    sshHost: String,
    sshPort: Int = 22,
    username: String,
    password: String,
    baseDirectory: String = ""
) : FileManager {
    private var sftpClient: SftpClient? = null

    init {
        try {
           sftpClient = SftpClient(sshHost, sshPort, username, password, baseDirectory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun saveFile(file: File, directory: String) {
        sftpClient!!.uploadFile(file, directory)
    }

    override fun moveFile(file: File, directory: String) {
        sftpClient!!.uploadFileAndDelete(file, directory)
    }
}