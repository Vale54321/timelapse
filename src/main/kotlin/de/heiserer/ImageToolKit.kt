package de.heiserer

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class ImageToolKit {
    companion object {
        fun captureImage(rtspUrl: String, outputFolder: String) {

            val dateFormatFolderName = SimpleDateFormat("dd_MM_yyyy")
            val timestampFolder = dateFormatFolderName.format(Date())

            val folder = File(outputFolder, timestampFolder)

            if (!folder.exists()) {
                folder.mkdirs()
            }

            val dateFormatFileName = SimpleDateFormat("HH_mm_ss")
            val timestamp = dateFormatFileName.format(Date())

            val imagePath = File(folder, "image_$timestamp.jpg").path


            val command = listOf("ffmpeg", "-i", rtspUrl, "-vframes", "1", imagePath)
            val process = ProcessBuilder(command).start()

            val exitCode = process.waitFor()
            if (exitCode != 0) {
                println("Failed to capture image with ffmpeg (exit code: $exitCode)")
                return
            }
            println("Image has been captured at $timestamp")
        }

        fun createVideoFromImages(sourceFolder: String, videoName: String) {
            val tempDir = copyAndRenameImages(sourceFolder) // Create temporary folder

            val outputPath = File("output")

            if(!outputPath.exists()) {
                outputPath.mkdirs()
            }

            val outputName = File(outputPath, videoName).path

            val commandList = listOf("ffmpeg", "-i", tempDir.path + "/image_%05d.jpg", "-c:v", "libx264", "-pix_fmt", "yuv420p", outputName)

            try {
                println(commandList)
                val process = ProcessBuilder(commandList)
                    .start()

                val exitCode = process.waitFor()
                if (exitCode != 0) {
                    println("Failed to create Video with ffmpeg (exit code: $exitCode)")
                    return
                }
            } catch (e: IOException) {
                throw IOException("Error creating temporary directory or running ffmpeg command", e)
            } finally {
                println("Video has been created at $outputName")
                tempDir.deleteRecursively() // Delete the temporary folder
                println("Temporary folder has been deleted")
            }
        }
    }
}

fun copyAndRenameImages(source: String): File {
    val sourceDir = File(source)
    if (!sourceDir.isDirectory) {
        throw IllegalArgumentException("Source path '$source' is not a directory")
    }

    val destinationDir = createTempDirectory()
    if (!destinationDir.isDirectory) {
        throw IOException("Failed to open temporary directory")
    }

    val counter = AtomicInteger(0)
    val sortedImages = sourceDir.listFiles { file ->
        file.isFile && file.extension.lowercase(Locale.getDefault()) in listOf("jpg", "jpeg")
    }?.sortedBy { Files.getLastModifiedTime(Paths.get(it.absolutePath)) } // Sort by last modified time

    sortedImages?.forEach { image ->
        val newFileName = "image_" + String.format("%05d", counter.getAndIncrement()) + ".jpg"
        val destinationFile = File(destinationDir, newFileName)
        image.copyTo(destinationFile, true)
    }

    return destinationDir
}

fun createTempDirectory(): File {
    val tempDir = File("tmp")
    if (tempDir.exists() && !tempDir.isDirectory) {
        throw IOException("tmp is no directory")
    }

    if (!tempDir.exists()) {
        tempDir.mkdirs()
    }

    val uniqueName = UUID.randomUUID().toString().substring(0, 6)
    val newDir = File(tempDir, uniqueName)
    if (!newDir.mkdirs()) {
        throw IOException("Failed to create temporary directory")
    }

    println("Temporary directory has been created at ${newDir.path}")
    return newDir
}