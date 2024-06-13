package de.heiserer.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
            createTimeLapse()
        }
    }
}

fun createFileList(folder: File): File {
    val outputFile = File("input.txt")
    val files = folder.listFiles { file -> file.extension == "jpg" }?.sorted() ?: emptyList()

    // Write the file paths to input.txt
    outputFile.printWriter().use { out ->
        files.forEach { file ->
            out.println("file '${file.absolutePath}'")
        }
    }

    println("input.txt has been created with ${files.size} entries.")
    return outputFile
}

fun createTimeLapse() {
    val dateFormatFolderName = SimpleDateFormat("dd_MM_yyyy")
    val timestampFolder = dateFormatFolderName.format(Date(System.currentTimeMillis())) // Yesterday's date

    val folder = File("images", timestampFolder)
    if (!folder.exists() || folder.listFiles().isNullOrEmpty()) {
        println("No images found for creating time-lapse")
        return
    }

    val inputImagesFile = createFileList(folder)

    val outputVideoPath = File(folder, "timelapse.mp4").path

    val command = listOf(
        "ffmpeg", "-f", "concat", "-safe", "0", "-i",
        inputImagesFile.path, "-c:v", "libx264", "-pix_fmt", "yuv420p", outputVideoPath
    )
    val process = ProcessBuilder(command).start()
    println("Executed command for time-lapse: ${command.joinToString(" ")}")

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        println("Failed to create time-lapse with ffmpeg (exit code: $exitCode)")
    }
}