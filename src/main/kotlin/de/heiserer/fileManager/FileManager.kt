package de.heiserer.fileManager

import java.io.File

interface FileManager {
    suspend fun saveFile(file: File, directory: String)
}