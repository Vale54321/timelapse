package de.heiserer.plugins

import de.heiserer.ImageToolKit
import de.heiserer.VideoSessionManager
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import io.ktor.http.HttpStatusCode
import java.util.*


fun Application.configureRouting() {


    routing {
        get("/") {
            val videoFolder = File("output")
            val fileFilter = { it: File -> it.name.endsWith(".mp4") }
            val videoFiles = videoFolder.listFiles(fileFilter)
            val videos = videoFiles?.map {
                TimeLapseVideo(id = it.name.hashCode(), name = it.nameWithoutExtension, path = "/download/${it.name}")
            } ?: emptyList()

            call.respond(ThymeleafContent("index2", mapOf("videos" to videos)))
        }

        // Add a route for downloading individual files (same as before)
        get("/download/{fileName}") {
            val fileName = call.parameters["fileName"] ?: return@get call.respond(HttpStatusCode.NotFound)
            val file = File("output", fileName) // Replace with actual path if needed
            if (file.exists()) {
                call.respondFile(file)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        webSocket("/videoProgress/{sessionId}") {
            // Handle WebSocket connection
            val sessionId = call.parameters["sessionId"] ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing sessionId"))
            val videoSession = VideoSessionManager.getSession(sessionId)

            if (videoSession == null) {
                close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Session $sessionId not found"))
                return@webSocket
            }

            try {
                // Continuously send progress updates to client
                videoSession.progressChannel.consumeEach { progress ->
                    send(Frame.Text("Progress: $progress%"))
                }
            } finally {
                // Clean up when client disconnects
                videoSession.removeWebSocketSession(this)
            }
        }

        get("/videoCreation") {
            val currentDate = getCurrentDate()

            val imagesFolder = "images/$currentDate"
            val frameRate = 30

            val uniqueId = UUID.randomUUID().toString().substring(0, 6)
            println("WebsocketId: $uniqueId")

            val videoSession = VideoSessionManager.startSession(uniqueId)
            call.respond(ThymeleafContent("videoCreation", mapOf("sessionId" to uniqueId)))


            videoSession.progressChannel.send("Video creation started for $currentDate")

            try {
                ImageToolKit.createVideoFromImages(imagesFolder, currentDate, frameRate, videoSession.progressChannel)
            } finally {
                // Clean up after completion
                VideoSessionManager.endSession(currentDate)
            }


        }
    }
}

data class TimeLapseVideo(val id: Int, val name: String, val path: String)

private fun getCurrentDate(): String {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy")
    return currentDate.format(formatter)
}


