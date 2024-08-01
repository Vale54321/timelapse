package de.heiserer.fileManager

import java.io.File

interface FileManager {
    fun saveFile(file: File, directory: String)
    fun moveFile(file: File, directory: String)
}