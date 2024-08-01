package de.heiserer.fileManager

import java.io.File
import java.io.IOException

class LocalFileManager : FileManager {
    override fun saveFile(file: File, directory: String) {
        val folder = File(directory)

        if (!folder.exists()) {
            folder.mkdirs()
        }

        val destinationFile = File(folder, file.name)

        try {
            // Copy the file to the destination folder
            file.copyTo(destinationFile)
        } catch (e: IOException) {
            println("Failed to copy file: ${e.message}")
        }
    }

    override fun moveFile(file: File, directory: String) {
        saveFile(file, directory)
        file.delete()
    }
}