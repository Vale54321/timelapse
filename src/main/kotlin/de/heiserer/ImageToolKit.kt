package de.heiserer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class ImageToolKit {
    companion object {
        suspend fun captureImage(rtspUrl: String, outputFolder: String) : File {
            println("captureImage at " + Date() )

            val folder = File(outputFolder)

            if (!folder.exists()) {
                folder.mkdirs()
            }

            val dateFormatFileName = SimpleDateFormat("HH_mm_ss")
            val timestamp = dateFormatFileName.format(Date())

            val imageFile = File(folder, "image_$timestamp.jpg")


            val command = listOf("ffmpeg", "-i", rtspUrl, "-vframes", "1", imageFile.path)

            val process = withContext(Dispatchers.IO) {
                ProcessBuilder(command).start()
            }

            val exitCode = withContext(Dispatchers.IO) {
                process.waitFor()
            }

            if (exitCode != 0) {
                println("Failed to capture image with ffmpeg (exit code: $exitCode)")
                throw IOException("Failed to capture image with ffmpeg (exit code: $exitCode)")
            }

            return imageFile
        }

        suspend fun createVideoFromImages(sourceFolder: String, videoName: String, fps: Int, progressChannel: SendChannel<String>): String {
            val tempDir = copyAndRenameImages(sourceFolder)

            val outputPath = File("output")
            if (!outputPath.exists()) {
                outputPath.mkdirs()
            }

            val outputName = videoName + "_$fps.mp4"
            val outputFile = File(outputPath, outputName).path
            val encoder = System.getenv("FFMPEG_ENCODER") ?: "libx264"

            println("Encoder: $encoder")

            val commandList = listOf(
                "ffmpeg", "-r", fps.toString(), "-i", "${tempDir.path}/image_%05d.jpg", "-c:v", encoder,
                "-crf", "35", "-y", outputFile
            )

            val fileCount = tempDir.listFiles()?.count { it.isFile } ?: 0

            try {
                val process = withContext(Dispatchers.IO) {
                    ProcessBuilder(commandList)
                        .redirectErrorStream(true)
                        .start()
                }

                // Read output of ffmpeg process
                val reader = process.inputStream.bufferedReader()
                var line: String? = withContext(Dispatchers.IO) {
                    reader.readLine()
                }
                while (line != null) {
                    // Assuming ffmpeg outputs progress lines that can be parsed
                    // You should adjust this based on ffmpeg's actual output
                    if (line.contains("frame=")) {
                        val progress = parseProgressFromFfmpegOutput(line)
                        val progressPercentage = (progress.toDouble() / fileCount) * 100

                        progressChannel.send(progressPercentage.toString())
                    }
                    line = withContext(Dispatchers.IO) {
                        reader.readLine()
                    }
                }

                val exitCode = withContext(Dispatchers.IO) {
                    process.waitFor()
                }
                if (exitCode != 0) {
                    println("Failed to create Video with ffmpeg (exit code: $exitCode)")
                    throw Exception("Error executing ffmpeg")
                }
            } catch (e: IOException) {
                throw IOException("Error creating temporary directory or running ffmpeg command", e)
            } finally {
                println("Video has been created at $outputName")
                tempDir.deleteRecursively() // Delete the temporary folder
                println("Temporary folder has been deleted")
            }
            return videoName
        }
    }
}


private fun parseProgressFromFfmpegOutput(line: String): Int {
    // Parse the progress percentage from ffmpeg output
    // Example line: frame= 1000 fps=30.0 q=24.0 size= 4200kB time=00:00:33.33 bitrate=1024.0kbits/s speed=1.50x
    val regex = Regex("frame=\\s*(\\d+)")
    val matchResult = regex.find(line)
    return matchResult?.groupValues?.get(1)?.toIntOrNull() ?: 0
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