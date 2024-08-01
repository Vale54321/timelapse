package de.heiserer.fileManager

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.File
import java.time.LocalDateTime

class SshFileManager(
    private val sshHost: String,
    private val sshPort: Int = 22,
    private val username: String,
    private val password: String,
    private val baseDirectory: String = ""
) : FileManager {
    private val jsch = JSch()
    private var session: Session? = null
    private var channelSftp: ChannelSftp? = null

    init {


        try {
            // Create a session
            session = jsch.getSession(username, sshHost, sshPort)
            session?.setPassword(password)

            // Avoid asking for key confirmation
            session?.setConfig("StrictHostKeyChecking", "no")
            session?.connect()

            // Create SFTP channel
            channelSftp = session?.openChannel("sftp") as ChannelSftp
            channelSftp?.connect()

            createRemoteDirectory(baseDirectory)

            channelSftp!!.cd(baseDirectory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun saveFile(file: File, directory: String) {
        if (file.exists()) {
            createRemoteDirectory(directory)

            val remoteFilePath = directory + File.separator + file.name

            channelSftp!!.put(file.path, remoteFilePath)
            println("Image Uploaded at: ${LocalDateTime.now()} to $remoteFilePath")
        } else {
            println("File ${file.path} does not exist.")
        }
    }

    private fun createRemoteDirectory(remoteDirectory: String) {
        try {
            val directories = remoteDirectory.split("/")
            var currentPath = channelSftp!!.pwd()

            for (dir in directories) {
                if (dir.isNotEmpty()) {
                    currentPath += "/$dir"
                    try {
                        channelSftp!!.ls(currentPath) // Check if the directory exists
                    } catch (e: Exception) {
                        println("MKDIRR $currentPath")
                        channelSftp!!.mkdir(currentPath) // Create the directory if it does not exist
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // Handle exceptions, consider logging instead
        }
    }
}