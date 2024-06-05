package de.heiserer

import de.heiserer.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val rtspUrl = "rtsps://10.0.0.1:7441/mstHSxlikG8CrMfh?enableSrtp"
    val outputFolder = "images"

    launch {
        while (true) {
            captureImage(rtspUrl, outputFolder)
            delay(5000) // Delay for 1 second
        }
    }

    configureRouting() // Existing routing configuration
}

fun captureImage(rtspUrl: String, outputFolder: String) {
    val timestamp = System.currentTimeMillis().toString()
    val imagePath = File(outputFolder, "image_$timestamp.jpg").absolutePath

    val command = listOf("ffmpeg", "-i", rtspUrl, "-vframes", "1", imagePath)
    val process = ProcessBuilder(command).start()
    println("Executed command: ${command.joinToString(" ")}")

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        println("Failed to capture image with ffmpeg (exit code: $exitCode)")
    }
}
